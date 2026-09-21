package songoycollection.article.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;
import songoycollection.article.dtos.ArticleRequestDto;
import songoycollection.article.dtos.ArticleTypeDto;
import songoycollection.article.dtos.ArticleTypeRequestDto;
import songoycollection.article.dtos.ArticleVariantDto;
import songoycollection.article.dtos.ArticleVariantRequestDto;
import songoycollection.article.dtos.ArticleContextDto;
import songoycollection.article.dtos.DeclinaisonDto;
import songoycollection.article.dtos.DeclinaisonRequestDto;
import songoycollection.article.dtos.SimpleArticleResponse;
import songoycollection.article.dtos.ModeleArticleDto;
import songoycollection.article.dtos.ModeleArticleRequestDto;
import songoycollection.article.model.Declinaison;
import songoycollection.article.model.DeclinaisonAttribute;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.model.ArticleVariantAttribute;
import songoycollection.category.model.CategoryAttribute;
import songoycollection.category.model.CategoryAttributeLevel;
import songoycollection.article.model.ArticleType;
import songoycollection.article.model.Article;
import songoycollection.article.model.ModeleArticle;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    @Transactional
    public ModeleArticleDto addModeleArticle(
            Long articleId,
            ModeleArticleRequestDto request,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        if (request == null || request.getNom() == null || request.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du modèle est obligatoire.");
        }

        Article article = repo.getArticleRepository().findByIdAndActiveTrue(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));

        ModeleArticle modeleArticle = new ModeleArticle();
        modeleArticle.setArticle(article);
        modeleArticle.setNom(request.getNom().trim());
        modeleArticle.setMarque(trimToNull(request.getMarque()));
        modeleArticle.setMatiere(trimToNull(request.getMatiere()));

        Set<ConstraintViolation<ModeleArticle>> violations = validator.validate(modeleArticle);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        ModeleArticle savedModele = repo.getModeleArticleRepository().save(modeleArticle);
        return ModeleArticleDto.from(savedModele);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Transactional
    public DeclinaisonDto addDeclinaison(
            Long modeleArticleId,
            DeclinaisonRequestDto request,
            MultipartFile image,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        ModeleArticle modeleArticle = repo.getModeleArticleRepository().findById(modeleArticleId)
                .orElseThrow(() -> new IllegalArgumentException("Modèle d'article introuvable."));
        Article article = modeleArticle.getArticle();
        if (!article.isActive()) {
            throw new IllegalArgumentException("L'article associé au modèle est inactif.");
        }

        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdAndNiveauOrderByOrdreAscIdAsc(
                        article.getCategory().getId(),
                        CategoryAttributeLevel.DECLINAISON
                );
        Map<Long, CategoryAttribute> allowedAttributes = new HashMap<>();
        definitions.forEach(definition ->
                allowedAttributes.put(definition.getAttribute().getId(), definition)
        );

        Map<Long, String> requestedValues = request == null || request.getAttributs() == null
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
                        "Cette caractéristique n'est pas configurée pour les déclinaisons de l'article."
                );
            }
        }

        Map<Long, String> valuesToSave = new LinkedHashMap<>();
        requestedValues.forEach((attributeId, value) -> {
            if (value != null && !value.isBlank()) {
                valuesToSave.put(attributeId, value.trim());
            }
        });

        List<Path> createdFiles = new ArrayList<>();
        try {
            Declinaison declinaison = new Declinaison();
            declinaison.setModeleArticle(modeleArticle);

            Path imagePath = saveDeclinaisonImage(image, createdFiles);
            if (imagePath != null) {
                declinaison.setUrlImage(imagePath.getFileName().toString());
            }

            Declinaison savedDeclinaison = repo.getDeclinaisonRepository().save(declinaison);
            List<DeclinaisonAttribute> declinaisonAttributes = new ArrayList<>();
            valuesToSave.forEach((attributeId, value) -> {
                DeclinaisonAttribute declinaisonAttribute = new DeclinaisonAttribute();
                declinaisonAttribute.setDeclinaison(savedDeclinaison);
                declinaisonAttribute.setAttribute(
                        allowedAttributes.get(attributeId).getAttribute()
                );
                declinaisonAttribute.setValue(value);
                declinaisonAttributes.add(declinaisonAttribute);
            });
            repo.getDeclinaisonAttributeRepository().saveAll(declinaisonAttributes);

            return DeclinaisonDto.from(savedDeclinaison, valuesToSave, true);
        } catch (Exception exception) {
            deleteFiles(createdFiles);
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            log.error("Erreur lors de l'enregistrement de la déclinaison du modèle {}", modeleArticleId, exception);
            throw new IllegalStateException("Impossible d'enregistrer la déclinaison.", exception);
        }
    }

    @Transactional
    public DeclinaisonDto updateDeclinaison(
            Long declinaisonId,
            DeclinaisonRequestDto request,
            MultipartFile image,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );

        Declinaison declinaison = repo.getDeclinaisonRepository().findById(declinaisonId)
                .orElseThrow(() -> new IllegalArgumentException("Déclinaison introuvable."));
        ModeleArticle modeleArticle = declinaison.getModeleArticle();
        Article article = modeleArticle.getArticle();
        if (!article.isActive()) {
            throw new IllegalArgumentException("L'article associé à la déclinaison est inactif.");
        }

        List<DeclinaisonAttribute> existingAttributes = repo.getDeclinaisonAttributeRepository()
                .findAllByDeclinaisonIdsWithAttribute(List.of(declinaisonId));
        Map<Long, String> existingValues = new LinkedHashMap<>();
        existingAttributes.forEach(attribute -> existingValues.put(
                attribute.getAttribute().getId(), attribute.getValue().trim()
        ));

        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdAndNiveauOrderByOrdreAscIdAsc(
                        article.getCategory().getId(),
                        CategoryAttributeLevel.DECLINAISON
                );
        Map<Long, CategoryAttribute> allowedAttributes = new HashMap<>();
        definitions.forEach(definition ->
                allowedAttributes.put(definition.getAttribute().getId(), definition)
        );

        boolean attributesProvided = request != null && request.getAttributs() != null;
        Map<Long, String> requestedValues = attributesProvided
                ? request.getAttributs()
                : existingValues;
        if (attributesProvided) {
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
                            "Cette caractéristique n'est pas configurée pour les déclinaisons de l'article."
                    );
                }
            }
        }

        Map<Long, String> valuesToSave = new LinkedHashMap<>();
        requestedValues.forEach((attributeId, value) -> {
            if (value != null && !value.isBlank()) {
                valuesToSave.put(attributeId, value.trim());
            }
        });

        boolean hasVariants = repo.getArticleVariantRepository()
                .existsByDeclinaisonId(declinaisonId);
        boolean attributesChanged = !existingValues.equals(valuesToSave);
        if (hasVariants && attributesChanged) {
            throw new IllegalArgumentException(
                    "Les caractéristiques de cette déclinaison ne peuvent plus être modifiées, "
                            + "car elle possède déjà des variantes. Vous pouvez toutefois remplacer son image."
            );
        }

        List<Path> createdFiles = new ArrayList<>();
        String oldImage = declinaison.getUrlImage();
        boolean imageChanged = false;
        try {
            Path imagePath = saveDeclinaisonImage(image, createdFiles);
            if (imagePath != null) {
                declinaison.setUrlImage(imagePath.getFileName().toString());
                imageChanged = true;
            }

            if (!hasVariants && attributesProvided && attributesChanged) {
                repo.getDeclinaisonAttributeRepository().deleteAll(existingAttributes);
                repo.getDeclinaisonAttributeRepository().flush();
                List<DeclinaisonAttribute> replacements = new ArrayList<>();
                valuesToSave.forEach((attributeId, value) -> {
                    DeclinaisonAttribute replacement = new DeclinaisonAttribute();
                    replacement.setDeclinaison(declinaison);
                    replacement.setAttribute(allowedAttributes.get(attributeId).getAttribute());
                    replacement.setValue(value);
                    replacements.add(replacement);
                });
                repo.getDeclinaisonAttributeRepository().saveAll(replacements);
            }

            Declinaison savedDeclinaison = repo.getDeclinaisonRepository().save(declinaison);
            if (imageChanged) {
                String previousImage = oldImage;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (previousImage != null && !previousImage.isBlank()) {
                            deleteProcessedImage(previousImage, "article-variants");
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            deleteFiles(createdFiles);
                        }
                    }
                });
            }

            Map<Long, String> responseValues = attributesChanged ? valuesToSave : existingValues;
            return DeclinaisonDto.from(savedDeclinaison, responseValues, !hasVariants);
        } catch (Exception exception) {
            deleteFiles(createdFiles);
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            log.error("Erreur lors de la modification de la déclinaison {}", declinaisonId, exception);
            throw new IllegalStateException("Impossible de modifier la déclinaison.", exception);
        }
    }

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

    @Transactional
    public Object addArticleVariant(
            Long declinaisonId,
            ArticleVariantRequestDto request,
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

        Declinaison declinaison = repo.getDeclinaisonRepository().findById(declinaisonId)
                .orElseThrow(() -> new IllegalArgumentException("Déclinaison introuvable."));
        Article article = declinaison.getModeleArticle().getArticle();
        if (!article.isActive()) {
            throw new IllegalArgumentException("L'article associé à la déclinaison est inactif.");
        }

        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdAndNiveauOrderByOrdreAscIdAsc(
                        article.getCategory().getId(),
                        CategoryAttributeLevel.VARIANTE
                );
        Map<Long, CategoryAttribute> allowedAttributes = new HashMap<>();
        definitions.forEach(item -> allowedAttributes.put(item.getAttribute().getId(), item));

        Map<Long, String> requestedValues = getLongStringMap(request, definitions, allowedAttributes);

        ArticleVariant variant = new ArticleVariant();
        variant.setDeclinaison(declinaison);
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

        repo.getArticleVariantRepository().save(variant);
        return HttpCustumMessage.addMessage();
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

    public List<ModeleArticleDto> getModelesArticle(Long articleId, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );
        repo.getArticleRepository().findByIdAndActiveTrue(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article introuvable."));

        return repo.getModeleArticleRepository()
                .findByArticleIdOrderByIdAsc(articleId)
                .stream()
                .map(ModeleArticleDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeclinaisonDto> getDeclinaisonsModele(Long modeleArticleId, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(
                idAdmin,
                EnumShared.PersonnelRole.ADMIN
        );
        ModeleArticle modeleArticle = repo.getModeleArticleRepository().findById(modeleArticleId)
                .orElseThrow(() -> new IllegalArgumentException("Modèle d'article introuvable."));
        if (!modeleArticle.getArticle().isActive()) {
            throw new IllegalArgumentException("L'article associé au modèle est inactif.");
        }

        List<Declinaison> declinaisons = repo.getDeclinaisonRepository()
                .findByModeleArticleIdOrderByIdAsc(modeleArticleId);
        if (declinaisons.isEmpty()) {
            return List.of();
        }

        List<Long> declinaisonIds = declinaisons.stream()
                .map(Declinaison::getId)
                .toList();
        Map<Long, Map<Long, String>> attributsParDeclinaison = new HashMap<>();
        Set<Long> declinaisonsAvecVariantes = new HashSet<>(
                repo.getArticleVariantRepository().findDeclinaisonIdsWithVariants(declinaisonIds)
        );
        repo.getDeclinaisonAttributeRepository()
                .findAllByDeclinaisonIdsWithAttribute(declinaisonIds)
                .forEach(attributeValue -> attributsParDeclinaison
                        .computeIfAbsent(
                                attributeValue.getDeclinaison().getId(),
                                ignored -> new LinkedHashMap<>()
                        )
                        .put(attributeValue.getAttribute().getId(), attributeValue.getValue()));

        return declinaisons.stream()
                .map(declinaison -> {
                    DeclinaisonDto dto = DeclinaisonDto.from(
                            declinaison,
                            attributsParDeclinaison.getOrDefault(declinaison.getId(), Map.of()),
                            !declinaisonsAvecVariantes.contains(declinaison.getId())
                    );
                    dto.setVariants(getVariantsWithStock(declinaison.getId()));
                    return dto;
                })
                .toList();
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

    public List<ArticleVariantDto> getDeclinaisonVariants(Long declinaisonId, Long idAdmin) {
        personnelUtiliShared.getPersonnelIsActiveById(idAdmin);
        repo.getDeclinaisonRepository().findById(declinaisonId)
                .orElseThrow(() -> new IllegalArgumentException("Déclinaison introuvable."));

        return getVariantsWithStock(declinaisonId);
    }

    private List<ArticleVariantDto> getVariantsWithStock(Long declinaisonId) {
        Map<Long, Double> quantities = new HashMap<>();
        repo.getStockLotRepository().sumQuantitiesByVariantForDeclinaison(declinaisonId)
                .forEach(row -> quantities.put((Long) row[0], ((Number) row[1]).doubleValue()));
        List<ArticleVariantDto> variants = ArticleVariantDto.from(
                repo.getArticleVariantRepository().findByDeclinaisonIdOrderByIdAsc(declinaisonId)
        );
        variants.forEach(variant -> variant.setQuantity(quantities.getOrDefault(variant.getId(), 0.0)));
        return variants;
    }

    @Transactional
    public Object deleteArticleVariant(Long variantId, Long idAdmin) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);

        ArticleVariant variant = repo.getArticleVariantRepository().findForStockUpdate(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));

        boolean hasHistory = repo.getStockMouvementRepository().existsByVariantId(variantId)
                || repo.getStockLotRepository().existsByVariantId(variantId)
                || repo.getLigneAchatRepository().existsByVariantId(variantId)
                || repo.getLigneVenteRepository().existsByVariantId(variantId);
        if (hasHistory) {
            throw new IllegalArgumentException(
                    "Suppression impossible : cette variante possède déjà un mouvement, un lot "
                            + "ou un historique d'achat ou de vente."
            );
        }

        repo.getArticleVariantRepository().delete(variant);
        return HttpCustumMessage.deleteMessage();
    }

    @Transactional
    public Object updateArticleVariant(
            Long variantId,
            ArticleVariantRequestDto request,
            MultipartFile image,
            Long idAdmin
    ) {
        personnelUtiliShared.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        if (request == null || request.getPrixVente() == null || request.getPrixVente() <= 0) {
            throw new IllegalArgumentException("Un prix de vente valide est obligatoire.");
        }
        if (image != null && !image.isEmpty()) {
            throw new IllegalArgumentException(
                    "L'image est rattachee a la declinaison et ne peut pas etre modifiee depuis une variante."
            );
        }

        ArticleVariant variant = repo.getArticleVariantRepository().findForStockUpdate(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        List<CategoryAttribute> definitions = repo.getCategoryAttributeRepository()
                .findByCategoryIdAndNiveauOrderByOrdreAscIdAsc(
                        variant.getDeclinaison().getModeleArticle().getArticle().getCategory().getId(),
                        CategoryAttributeLevel.VARIANTE
                );
        Map<Long, CategoryAttribute> allowed = new HashMap<>();
        definitions.forEach(item -> allowed.put(item.getAttribute().getId(), item));

        Map<Long, String> requestedValues = request.getAttributs() == null
                ? Map.of()
                : request.getAttributs();
        for (Long attributeId : requestedValues.keySet()) {
            if (!allowed.containsKey(attributeId)) {
                throw new IllegalArgumentException("Caracteristique invalide pour cet article.");
            }
        }

        Map<Long, String> originalValues = new HashMap<>();
        variant.getAttributs().forEach(item -> {
            Long attributeId = item.getAttribute().getId();
            if (item.getValue() != null) {
                originalValues.put(attributeId, item.getValue().trim());
            }
        });
        Map<Long, String> proposedValues = new HashMap<>(originalValues);
        requestedValues.forEach((attributeId, value) -> {
            if (value == null || value.isBlank()) {
                proposedValues.remove(attributeId);
            } else {
                proposedValues.put(attributeId, value.trim());
            }
        });

        for (CategoryAttribute definition : definitions) {
            String value = proposedValues.get(definition.getAttribute().getId());
            if (definition.isObligatoire() && (value == null || value.isBlank())) {
                throw new IllegalArgumentException(
                        "La caracteristique obligatoire "
                                + definition.getAttribute().getLabel()
                                + " est manquante."
                );
            }
        }

        String requestedReference = request.getReference() == null
                ? variant.getReference()
                : request.getReference().trim();
        if (requestedReference == null || requestedReference.isBlank()) {
            throw new IllegalArgumentException("La reference de la variante est obligatoire.");
        }
        boolean referenceChanged = !requestedReference.equals(variant.getReference());
        boolean identityChanged = referenceChanged || !proposedValues.equals(originalValues);
        boolean hasStockOrMovements = repo.getStockLotRepository().existsByVariantId(variantId)
                || repo.getStockMouvementRepository().existsByVariantId(variantId);
        if (hasStockOrMovements && identityChanged) {
            throw new IllegalArgumentException(
                    "La reference et les caracteristiques d'une variante ayant deja du stock "
                            + "ou des mouvements ne peuvent pas etre modifiees."
            );
        }
        if (referenceChanged && repo.getArticleVariantRepository()
                .existsByReferenceIgnoreCaseAndIdNot(requestedReference, variantId)) {
            throw new IllegalArgumentException("Cette reference de variante existe deja.");
        }

        variant.setReference(requestedReference);
        variant.setPrixVente(request.getPrixVente());
        for (ArticleVariantAttribute item : new ArrayList<>(variant.getAttributs())) {
            Long attributeId = item.getAttribute().getId();
            String value = proposedValues.remove(attributeId);
            if (value == null) {
                variant.getAttributs().remove(item);
            } else {
                item.setValue(value);
            }
        }
        proposedValues.forEach((attributeId, value) -> {
            ArticleVariantAttribute item = new ArticleVariantAttribute();
            item.setVariant(variant);
            item.setAttribute(allowed.get(attributeId).getAttribute());
            item.setValue(value);
            variant.getAttributs().add(item);
        });

        repo.getArticleVariantRepository().save(variant);
        return HttpCustumMessage.addMessage();
    }

    private Path saveDeclinaisonImage(MultipartFile image, List<Path> createdFiles) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier de la déclinaison doit être une image.");
        }
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("L'image de la déclinaison ne doit pas dépasser 5 Mo.");
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
