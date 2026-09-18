package songoycollection.vente.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaiementVenteHistorique {

    private Long id;
    private LocalDateTime date;
    private BigDecimal montant;
    private EnumShared.ModePaiement modePaiement;
}
