package songoycollection.dashboard.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;

/**
 * Produit classé selon les ventes réalisées sur la période du dashboard.
 *
 * <p>Le classement est calculé au niveau de l'article : les ventes de ses
 * différentes variantes sont regroupées.</p>
 */
@Data
public class DashboardTopProduitDto {

    private Long articleId;
    private String article;
    private String categorie;
    private EnumShared.UniteMesureCategory unite;
    private Double quantiteVendue;
    private BigDecimal chiffreAffaires;
    private BigDecimal prixMoyen;
    private Integer rang;
    private BigDecimal partChiffreAffaires;
}
