package songoycollection.vente.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class LigneVenteHistorique {

    private Long id;
    private Long variantId;
    private String referenceVariant;

    private Long articleId;
    private String article;

    private EnumShared.UniteMesureCategory unite;

    private Map<Long, String> attributs;

    private Double quantite;
    private BigDecimal prixVente;
    private Double totalLigne;
}