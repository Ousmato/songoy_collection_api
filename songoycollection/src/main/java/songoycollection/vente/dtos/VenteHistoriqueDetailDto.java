package songoycollection.vente.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VenteHistoriqueDetailDto extends VenteHistoriqueDto {

    private List<LigneVenteHistorique> lignes;
    private List<PaiementVenteHistorique> paiements;

    public static VenteHistoriqueDetailDto fromSummary(VenteHistoriqueDto summary) {
        VenteHistoriqueDetailDto detail = new VenteHistoriqueDetailDto();
        detail.setId(summary.getId());
        detail.setNumeroFacture(summary.getNumeroFacture());
        detail.setDate(summary.getDate());
        detail.setClient(summary.getClient());
        detail.setTotalVente(summary.getTotalVente());
        detail.setMontantPaye(summary.getMontantPaye());
        detail.setResteAPayer(summary.getResteAPayer());
        detail.setStatutPaiement(summary.getStatutPaiement());
        detail.setModePaiement(summary.getModePaiement());
        return detail;
    }
}
