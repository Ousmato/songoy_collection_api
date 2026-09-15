package songoycollection.stock.service;

import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.stock.dtos.InitialStockRequest;
import songoycollection.stock.model.*;
import songoycollection.stock.repository.*;

@Service
@RequiredArgsConstructor
public class InitialStockService {
    private final ArticleVariantRepository variants;
    private final StockLotRepository lots;
    private final PersonnelUtiliShared personnel;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public boolean isAvailable(Long variantId, Long idAdmin) {
        personnel.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        ArticleVariant variant = variants.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        validateVariant(variant);
        return !hasStockHistory(variantId);
    }

    @Transactional
    public void create(Long variantId, Long idAdmin, InitialStockRequest request) {
        personnel.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        ArticleVariant variant = variants.findForStockUpdate(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        validateVariant(variant);
        if (hasStockHistory(variantId)) {
            throw new IllegalArgumentException(
                    "Cette variante possède déjà une entrée de stock. Le stock initial ne peut plus être ajouté."
            );
        }
        validateQuantity(variant, request);
        StockLot lot = createLot(variant, idAdmin, request);
        saveMovement(variant, lot, request);
    }

    private boolean hasStockHistory(Long variantId) {
        return lots.existsByVariantId(variantId);
    }

    private void validateVariant(ArticleVariant variant) {
        if (!variant.getArticle().isActive() || !variant.getArticle().getCategory().isActive()) {
            throw new IllegalArgumentException("L'article ou sa catégorie est inactif.");
        }
    }

    private void validateQuantity(ArticleVariant variant, InitialStockRequest request) {
        var unit = variant.getArticle().getCategory().getMesureCategory();
        if (unit == null) {
            throw new IllegalArgumentException("L'unité de la catégorie doit être renseignée.");
        }
        if (unit != EnumShared.UniteMesureCategory.METRE
                && request.quantity().stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("La quantité doit être un nombre entier pour cette unité.");
        }
    }

    private StockLot createLot(ArticleVariant variant, Long idAdmin, InitialStockRequest request) {
        StockLot lot = new StockLot();
        lot.setVariant(variant);
        lot.setInitialQuantity(request.quantity().doubleValue());
        lot.setQuantityRestante(request.quantity().doubleValue());
        lot.setPersonnel(personnel.getPersonnelById(idAdmin));
        lot.setEntryDate(request.date());
        lot.setCreatedAt(LocalDateTime.now());
        lot.setObservation(request.observation() == null ? null : request.observation().trim());
        return lots.save(lot);
    }

    private void saveMovement(ArticleVariant variant, StockLot lot, InitialStockRequest request) {
        StockMouvement movement = new StockMouvement();
        movement.setVariant(variant);
        movement.setStockLot(lot);
        movement.setQuantite(request.quantity().doubleValue());
        movement.setType(EnumShared.TypeMouvement.INVENTAIRE);
        movement.setReference("INITIAL-" + variant.getId());
        movement.setDateEntree(request.date().atStartOfDay());
        entityManager.persist(movement);
    }
}
