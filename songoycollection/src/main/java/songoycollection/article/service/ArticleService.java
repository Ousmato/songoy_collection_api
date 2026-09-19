package songoycollection.article.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;
import songoycollection.article.dtos.ArticleRequestDto;
import songoycollection.article.dtos.ArticleTypeDto;
import songoycollection.article.dtos.ArticleTypeRequestDto;
import songoycollection.article.dtos.ArticleVariantDto;
import songoycollection.article.dtos.ArticleVariantRequestDto;
import songoycollection.article.dtos.ArticleContextDto;
import songoycollection.article.dtos.SimpleArticleResponse;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.model.ArticleVariantAttribute;
import songoycollection.category.model.CategoryAttribute;
import songoycollection.article.model.ArticleType;
import songoycollection.article.model.Article;
import songoycollection.article.model.ArticleImage;
import songoycollection.category.model.Category;
import songoycollection.fileManagement.FileManagerService;
import songoycollection.fileManagement.ArticleImageProcessingService;
import songoycollection.article.shared.ArticleSharedUtilService;
import songoycollection.config.HttpCustumMessage;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    private final RepositoryService repo;
    private final PersonnelUtiliShared personnelUtiliShared;

    private final Validator validator;
    private final FileManagerService fileManager;
    private final ArticleImageProcessingService imageProcessing;
    private final EntityManager entityManager;

    public Object addArticleType(ArticleTypeRequestDto req, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        ArticleType newArticleType = ArticleTypeRequestDto.toArticleType(req);
        Set<ConstraintViolation<ArticleType>> violations = validator.validate(newArticleType);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        repo.getArticleTypeRepository().save(newArticleType);
        return HttpCustumMessage.addMessage();

    }

    public Object addArticleVariant(
            Long articleId,
            ArticleVariantRequestDto request,
            MultipartFile image,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        if (request == null || request.getReference() == null
                || request.getReference().isBlank()
                || request.getPrixVente() == null || request.getPrixVente() <= 0) {
            throw new IllegalArgumentException(
                    "La référence et un prix de vente valide sont obligatoires."
            );
        }

        String reference = request.getReference().trim();

        if (repo.getArticleVariantRepository().existsByReferenceIgnoreCase(reference)) {
            throw new IllegalArgumentException(
                    "Cette référence de variante existe déjà."
            );
        }

        Article article = repo.getArticleRepository().findByIdAndActiveTrue(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));
        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdOrderByOrdreAscIdAsc(article.getCategory().getId());
        Map<Long, CategoryAttribute> allowedAttributes = new HashMap<>();
        definitions.forEach(item -> allowedAttributes.put(item.getAttribute().getId(), item));

        Map<Long, String> requestedValues = getLongStringMap(request, definitions, allowedAttributes);

        ArticleVariant variant = new ArticleVariant();
        variant.setArticle(article);
        variant.setReference(reference);
        variant.setPrixVente(request.getPrixVente());
        requestedValues.forEach((attributeId, value) -> {
            if (value != null && !value.isBlank()) {
                ArticleVariantAttribute variantAttribute = new ArticleVariantAttribute();
                variantAttribute.setVariant(variant);
                variantAttribute.setAttribute(allowedAttributes.get(attributeId).getAttribute());
                variantAttribute.setValue(value.trim());
                variant.getAttributs().add(variantAttribute);
            }
        });

        List<Path> createdFiles = new ArrayList<>();
        try {
            Path imagePath = saveVariantImage(image, createdFiles);
            if (imagePath != null) {
                variant.setUrlImage(imagePath.getFileName().toString());
            }
            repo.getArticleVariantRepository().save(variant);
            return HttpCustumMessage.addMessage();
        } catch (Exception exception) {
            deleteFiles(createdFiles);
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            log.error("Erreur lors de l'enregistrement de l'image de la variante {}", reference, exception);
            throw new IllegalStateException("Impossible d'enregistrer l'image de la variante.", exception);
        }
    }

    public ArticleContextDto getArticleContext(Long articleId, Long idAdmin) {
        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        Article article = repo.getArticleRepository().findByIdAndActiveTrue(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));
        ArticleContextDto dto = new ArticleContextDto();
        dto.setId(article.getId());
        dto.setCategoryId(article.getCategory().getId());
        dto.setCategoryNom(article.getCategory().getNom());
        dto.setTypeNom(article.getArticleType().getNom());
        dto.setAttributes(repo.getCategoryAttributeRepository()
                .findByCategoryIdOrderByOrdreAscIdAsc(article.getCategory().getId())
                .stream()
                .map(songoycollection.category.dtos.AttributeDto::createCategoryAttributeDto)
                .toList());
        return dto;
    }

    private Map<Long, String> getLongStringMap(ArticleVariantRequestDto request, List<CategoryAttribute> definitions, Map<Long, CategoryAttribute> allowedAttributes) {
        Map<Long, String> requestedValues = request.getAttributs() == null
                ? Map.of()
                : request.getAttributs();
        for (CategoryAttribute definition : definitions) {
            String value = requestedValues.get(definition.getAttribute().getId());

            if (definition.isObligatoire() && (value == null || value.isBlank())) {
                throw new IllegalArgumentException(
                        "La caractéristique obligatoire « "
                                + definition.getAttribute().getLabel()
                                + " » est manquante."
                );
            }
        }

        for (Long attributeId : requestedValues.keySet()) {
            if (!allowedAttributes.containsKey(attributeId)) {
                throw new IllegalArgumentException(
                        "Cette caractéristique ne correspond pas à la catégorie de l'article."
                );
            }
        }

        return requestedValues;
    }

    public List<ArticleVariantDto> getArticleVariants(Long articleId, Long idAdmin) {
        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        if (!repo.getArticleRepository().existsById(articleId)) {
            throw new IllegalArgumentException("Article introuvable.");
        }
        Map<Long, Double> quantities = new HashMap<>();
        repo.getStockLotRepository().sumQuantitiesByVariantForArticle(articleId)
                .forEach(row -> quantities.put((Long) row[0], ((Number) row[1]).doubleValue()));
        List<ArticleVariantDto> variants = ArticleVariantDto.from(
                repo.getArticleVariantRepository().findByArticleIdOrderByIdAsc(articleId)
        );
        variants.forEach(variant -> variant.setQuantity(quantities.getOrDefault(variant.getId(), 0.0)));
        return variants;
    }

    public Object updateArticleVariant(
            Long variantId,
            ArticleVariantRequestDto request,
            MultipartFile image,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        if (request == null || request.getAttributs() == null
                || request.getPrixVente() == null || request.getPrixVente() <= 0) {
            throw new IllegalArgumentException("Les caractéristiques de la variante sont obligatoires.");
        }
        ArticleVariant variant = repo.getArticleVariantRepository().findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        String previousImage = variant.getUrlImage();
        variant.setPrixVente(request.getPrixVente());
        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdOrderByOrdreAscIdAsc(variant.getArticle().getCategory().getId());
Map<Long, CategoryAttribute> allowed = new HashMap<>();
        definitions.forEach(item -> allowed.put(item.getAttribute().getId(), item));

        Map<Long, String> values = request.getAttributs();
for (Long attributeId : values.keySet()) {
            if (!allowed.containsKey(attributeId)) {
                throw new IllegalArgumentException("Caractéristique invalide pour cet article.");
            }
        }

        Map<Long, ArticleVariantAttribute> current = new HashMap<>();
        variant.getAttributs().forEach(item -> current.put(item.getAttribute().getId(), item));
        values.forEach((attributeId, value) -> {
            if (value == null || value.isBlank()) {
                ArticleVariantAttribute removed = current.remove(attributeId);
                if (removed != null) {
                    variant.getAttributs().remove(removed);
                }
                return;
            }
            ArticleVariantAttribute item = current.get(attributeId);
            if (item == null) {
                item = new ArticleVariantAttribute();
                item.setVariant(variant);
                item.setAttribute(allowed.get(attributeId).getAttribute());
                variant.getAttributs().add(item);
                current.put(attributeId, item);
            }
            item.setValue(value.trim());
        });

        for (CategoryAttribute definition : definitions) {
            ArticleVariantAttribute item = current.get(definition.getAttribute().getId());
if (definition.isObligatoire()
                    && (item == null || item.getValue() == null || item.getValue().isBlank())) {
                throw new IllegalArgumentException(
                        "La caractÃ©ristique obligatoire Â« "
                                + definition.getAttribute().getLabel()
                                + " Â» (ID "
                                + definition.getAttribute().getId()
                                + ") est manquante."
                );
            }
        }
        List<Path> createdFiles = new ArrayList<>();
        try {
            Path imagePath = saveVariantImage(image, createdFiles);
            if (imagePath != null) {
                variant.setUrlImage(imagePath.getFileName().toString());
            }
            repo.getArticleVariantRepository().save(variant);
            if (imagePath != null && previousImage != null
                    && !previousImage.equals(imagePath.getFileName().toString())) {
                deleteProcessedImage(previousImage, "article-variants");
            }
            return HttpCustumMessage.addMessage();
        } catch (Exception exception) {
            deleteFiles(createdFiles);
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            log.error("Erreur lors de la modification de l'image de la variante {}", variantId, exception);
            throw new IllegalStateException("Impossible d'enregistrer l'image de la variante.", exception);
        }
    }

    private Path saveVariantImage(MultipartFile image, List<Path> createdFiles) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier de la variante doit être une image.");
        }
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("L'image de la variante ne doit pas dépasser 5 Mo.");
        }

        Path original = fileManager.save(image, "article-variants");
        createdFiles.add(original);
        for (Path generated : imageProcessing.process(original)) {
            if (!createdFiles.contains(generated)) {
                createdFiles.add(generated);
            }
        }
        return original;
    }

    private void deleteFiles(List<Path> files) {
        files.forEach(path -> {
            try {
                fileManager.delete(path);
            } catch (Exception ignored) {
                // Le fichier temporaire ne doit pas masquer l'erreur principale.
            }
        });
    }

    private void deleteProcessedImage(String storedPath, String folder) {
        try {
            Path original = Path.of(storedPath);
            if (!original.isAbsolute()) {
                original = fileManager.resolve(folder, storedPath);
            }
            deleteFiles(List.of(
                    original,
                    original.resolveSibling(stem(original) + "-thumbnail.webp"),
                    original.resolveSibling(stem(original) + "-medium.webp"),
                    original.resolveSibling(stem(original) + "-large.webp")
            ));
        } catch (IOException | RuntimeException ignored) {
            // Une ancienne URL peut ne pas être un chemin local géré par l'application.
        }
    }

    private String stem(Path path) {
        String name = path.getFileName().toString();
        int extensionIndex = name.lastIndexOf('.');
        return extensionIndex > 0 ? name.substring(0, extensionIndex) : name;
    }

    public List<ArticleTypeDto> getAllArticleTypes(Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        List<ArticleType> articleTypes = repo.getArticleTypeRepository().findAll();
        return ArticleTypeDto.getArticleTypeDtos(articleTypes).stream().sorted(Comparator.comparing(ArticleType::getNom)).toList();
    }

    public List<SimpleArticleResponse> getAllArticles(Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        return repo.getArticleRepository().findByActiveTrueOrderByIdDesc().stream()
                .map(article -> new SimpleArticleResponse(
                        article.getId(),
                        article.getCategory().getNom(),
                        repo.getArticleVariantRepository()
                                .findMinimumPriceByArticleId(article.getId()),
                        article.getArticleType().getId(),
                        article.getArticleType().getNom(),
                        repo.getStockLotRepository()
                                .sumQuantityByArticleId(article.getId()),
                        article.getCategory().getMesureCategory()
                ))
                .toList();
    }

    public List<SimpleArticleResponse> getArticlesByCategory(Long categoryId, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        if (categoryId == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }

        return repo.getArticleRepository()
                .findByCategoryIdAndActiveTrueOrderByIdDesc(categoryId)
                .stream()
                .map(this::toSimpleArticleResponse)
                .toList();
    }

    private SimpleArticleResponse toSimpleArticleResponse(Article article) {
        return new SimpleArticleResponse(
                article.getId(),
                article.getCategory().getNom(),
                repo.getArticleVariantRepository()
                        .findMinimumPriceByArticleId(article.getId()),
                article.getArticleType().getId(),
                article.getArticleType().getNom(),
                repo.getStockLotRepository()
                        .sumQuantityByArticleId(article.getId()),
                article.getCategory().getMesureCategory()
        );
    }

    @Transactional
    public Object addArticle(ArticleRequestDto req, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);

        if (req == null || req.getCategoryId() == null || req.getArticleTypeId() == null) {
            throw new IllegalArgumentException("CatÃ©gorie et type d'article sont obligatoires.");
        }

        Category category = repo.getCategoryRepository().findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("CatÃ©gorie introuvable."));

        ArticleType articleType = repo.getArticleTypeRepository().findById(req.getArticleTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Type d'article introuvable."));

        boolean duplicate = repo.getArticleRepository().existsByCategoryIdAndArticleTypeId(
                req.getCategoryId(), req.getArticleTypeId());

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Cet article existe dÃ©jÃ  pour cette catÃ©gorie, ce type d'article."
            );
        }

        Article article = new Article();
        article.setCategory(category);
        article.setArticleType(articleType);
        article.setCreationDate(java.time.LocalDateTime.now());
        Set<ConstraintViolation<Article>> violations = validator.validate(article);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        repo.getArticleRepository().save(article);

        if (req.getFiles() != null && !req.getFiles().isEmpty()) {
            List<Path> createdFiles = new ArrayList<>();
            try {
                for (MultipartFile file : req.getFiles()) {
                    Path original = fileManager.save(file, "articles");
                    createdFiles.addAll(imageProcessing.process(original));
                    ArticleImage image = new ArticleImage();
                    image.setArticle(article);
                    image.setUrlImage(original.getFileName().toString());
                    entityManager.persist(image);
                }
            } catch (Exception exception) {
                for (Path path : createdFiles) {
                    try { fileManager.delete(path); } catch (Exception ignored) { }
                }
                log.error("Erreur lors de l'enregistrement des images de l'article {}", article.getId(), exception);
                throw new IllegalStateException("Impossible d'enregistrer les images de l'article.", exception);
            }
        }

        return HttpCustumMessage.addMessage();

    }

}
