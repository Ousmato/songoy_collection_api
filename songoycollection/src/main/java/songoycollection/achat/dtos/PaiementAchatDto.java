package songoycollection.achat.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.time.LocalDateTime;

/** Paiement effectivement versé au fournisseur. */
@Data
public class PaiementAchatDto {
    private Long id;
    private LocalDateTime date;
    private Double montant;
    private EnumShared.ModePaiement modePaiement;
}
