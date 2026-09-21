package songoycollection.vente.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LigneVenteHistorique {

    private Long id;
    private Long variantId;
    private String referenceVariant;

    private Long articleId;
    private String article;
    private String modele;
    private String marque;

    private EnumShared.UniteMesureCategory unite;

    private List<LigneVenteAttributHistoriqueDto> attributsDeclinaison;
    private List<LigneVenteAttributHistoriqueDto> attributsVariante;

    private Double quantite;
    private BigDecimal prixVente;
    private Double totalLigne;
}
