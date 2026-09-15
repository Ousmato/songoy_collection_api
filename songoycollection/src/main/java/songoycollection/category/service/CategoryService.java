package songoycollection.category.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.category.dtos.CategoryAttributeDto;
import songoycollection.category.dtos.CategoryResponseDto;
import songoycollection.category.dtos.CategoryAttributeRequestDto;
import songoycollection.category.model.CategoryAttribute;
import songoycollection.category.model.AttributeDefinition;
import songoycollection.category.model.Category;
import songoycollection.config.HttpCustumMessage;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final RepositoryService repo;
    private final PersonnelUtiliShared personnelUtiliShared;
    private final Validator validator;
    private final Object attributeDefinitionLock = new Object();

    public Object addCategory(Category category, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        repo.getCategoryRepository().save(category);
        return HttpCustumMessage.addMessage();
    }



    public List<CategoryResponseDto> getAllCategories(Long idAdmin) {

        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        List<Category> categories =
                repo.getCategoryRepository().findByActive(true);


        if (categories.isEmpty()) {
            return List.of();
        }

        List<CategoryResponseDto> categoryResponseDtoList = CategoryResponseDto.fromCategoryList(categories);
        List<Long> ids = categories.stream()
                .map(Category::getId)
                .toList();

        List<Object[]> articleCountCategory =
                repo.getArticleRepository().countArticlesByCategoryIds(ids);

        Map<Long, Long> articleCountByCategory = articleCountCategory.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        categoryResponseDtoList.forEach(category ->
                category.setNombreArticle(
                        articleCountByCategory.getOrDefault(category.getId(), 0L)
                )
        );

        return categoryResponseDtoList;
    }

    public void addCategoryAttribute(
            Long categoryId,
            CategoryAttributeRequestDto request,
            Long idAdmin
    ) {
        validateAdmin(idAdmin);
        validateAttributeRequest(request);

        Category category = findCategory(categoryId);
        AttributeDefinition definition = findOrCreateDefinition(request);
        associateAttribute(category, definition, request);
    }

    public Object addCategoryAttributes(
            CategoryAttributeRequestDto request,
            Long idAdmin
    ) {
        validateAdmin(idAdmin);
        validateCategoryIds(request);
        validateAttributeRequest(request);

        AttributeDefinition definition = findOrCreateDefinition(request);
        for (Long categoryId : request.getCategoryIds()) {
            associateAttribute(findCategory(categoryId), definition, request);
        }

        return HttpCustumMessage.addMessage();
    }

    private void validateAdmin(Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );
    }

    private void validateCategoryIds(CategoryAttributeRequestDto request) {
        if (request == null || request.getCategoryIds() == null
                || request.getCategoryIds().isEmpty()
                || request.getCategoryIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "Au moins une categorie valide doit etre selectionnee."
            );
        }
    }

    private void validateAttributeRequest(CategoryAttributeRequestDto request) {
        if (request == null || request.getKey() == null || request.getKey().isBlank()
                || request.getLabel() == null || request.getLabel().isBlank()
                || request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException(
                    "La cle, le libelle et le type de l'attribut sont obligatoires."
            );
        }

        String type = request.getType().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("TEXT", "NUMBER", "SELECT", "DATE").contains(type)) {
            throw new IllegalArgumentException("Type d'attribut invalide.");
        }
    }

    private Category findCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Identifiant de categorie invalide.");
        }
        return repo.getCategoryRepository().findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categorie introuvable."));
    }

    private AttributeDefinition findOrCreateDefinition(CategoryAttributeRequestDto request) {
        String key = request.getKey().trim().toLowerCase(Locale.ROOT);
        String type = request.getType().trim().toUpperCase(Locale.ROOT);

        synchronized (attributeDefinitionLock) {
            return repo.getAttributeDefinitionRepository()
                    .findByKey(key)
                    .orElseGet(() -> createDefinition(key, request.getLabel().trim(), type));
        }
    }

    private AttributeDefinition createDefinition(String key, String label, String type) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setLabel(label);
        definition.setType(type);
        return repo.getAttributeDefinitionRepository().save(definition);
    }

    private void associateAttribute(
            Category category,
            AttributeDefinition definition,
            CategoryAttributeRequestDto request
    ) {
        if (repo.getCategoryAttributeRepository()
                .existsByCategoryIdAndAttributeId(category.getId(), definition.getId())) {
            return;
        }

        CategoryAttribute association = new CategoryAttribute();
        association.setCategory(category);
        association.setAttribute(definition);
        association.setObligatoire(request.isObligatoire());
        association.setOrdre(request.getOrdre() == null ? 0 : request.getOrdre());

        Set<ConstraintViolation<CategoryAttribute>> violations = validator.validate(association);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        repo.getCategoryAttributeRepository().save(association);
    }

    public List<CategoryAttributeDto> getCategoryAttributes(Long categoryId, Long idAdmin) {
        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        if (!repo.getCategoryRepository().existsById(categoryId)) {
            throw new IllegalArgumentException("CatÃ©gorie introuvable.");
        }
        return List.of(CategoryAttributeDto.createCategoryAttributeDto(
                categoryId,
                repo.getCategoryRepository().findById(categoryId).orElseThrow().getNom(),
                repo.getCategoryAttributeRepository().findByCategoryIdOrderByOrdreAscIdAsc(categoryId)
        ));
    }

    public List<CategoryAttributeDto> getAllCategoryAttributes(Long idAdmin) {
        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        return repo.getCategoryRepository().findActiveWithAttributes().stream()
                .map(category -> CategoryAttributeDto.createCategoryAttributeDto(
                        category.getId(),
                        category.getNom(),
                        repo.getCategoryAttributeRepository()
                                .findByCategoryIdOrderByOrdreAscIdAsc(category.getId())
                ))
                .toList();
    }

}
