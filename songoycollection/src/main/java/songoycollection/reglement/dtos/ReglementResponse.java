package songoycollection.reglement.dtos;

import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglementResponse(
        Long id,
        Long achatId,
        Long venteId,
        BigDecimal montant,
        EnumShared.ModePaiement modePaiement,
        LocalDateTime date,
        BigDecimal resteAPayer
) {
}
