package songoycollection.achat.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.util.List;

/** Ligne détaillée avec la variante et le coût réellement réceptionné. */
@Data
public class LigneAchatHistoriqueDto {
    private Long id;
    private Long variantId;
    private String referenceVariant;
    private Long articleId;
    private String article;
    private String modele;
    private String marque;
    private List<LigneAchatAttributHistoriqueDto> attributsDeclinaison;
    private List<LigneAchatAttributHistoriqueDto> attributsVariante;
    private EnumShared.UniteMesureCategory unite;
    private Double quantite;
    private Double prixAchatUnitaire;
    private Double totalLigne;
}
