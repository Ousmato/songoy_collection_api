package songoycollection.dashboard.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

/** Alerte de stock calculée pour une variante sur une entité donnée. */
@Data
public class DashboardAlerteStockDto {

    private Long variantId;
    private Long articleId;
    private String article;
    private String categorie;
    private String reference;
    private EnumShared.UniteMesureCategory unite;
    private Double quantiteRestante;
    private String statut;
}
