package songoycollection.achat.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** Données complètes affichées lors de l'ouverture d'un achat. */
@Data
@EqualsAndHashCode(callSuper = true)
public class AchatHistoriqueDetailDto extends AchatHistoriqueDto {
    private FournisseurDto fournisseurDetail;
    private PersonnelAchatDto responsable;
    private List<LigneAchatHistoriqueDto> lignes;
    private List<PaiementAchatDto> paiements;
}
