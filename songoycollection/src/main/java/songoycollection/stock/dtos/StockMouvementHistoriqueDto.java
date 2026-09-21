package songoycollection.stock.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockMouvementHistoriqueDto {
    private Long idMouvement;
    private LocalDateTime date;
    private EnumShared.TypeMouvement type;

    private Long lotId;
    private Long articleId;
    private String article; // catégorie + type
    private String modele;
    private String marque;
    private Long variantId;
    private String referenceVariante;
    private List<StockMouvementAttributDto> attributsDeclinaison;
    private List<StockMouvementAttributDto> attributsVariante;
    private EnumShared.UniteMesureCategory unite;

    private Double quantite;
    private BigDecimal coutAchatUnitaire;
    private BigDecimal valeurMouvement; // quantité × coût d'achat du lot

    private String referenceMouvement; // valeur StockMouvement.reference
    private String numeroFacture;      // facture liée à l'achat ou à la vente
    private String tiers;              // fournisseur ou client
    private String responsable;        // responsable de l'achat ou caissier
}
