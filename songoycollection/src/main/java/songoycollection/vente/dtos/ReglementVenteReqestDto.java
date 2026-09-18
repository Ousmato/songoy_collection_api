package songoycollection.vente.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;
@Data
public class ReglementVenteReqestDto {
    private Double montant;
    private EnumShared.ModePaiement modePaiement;
}
