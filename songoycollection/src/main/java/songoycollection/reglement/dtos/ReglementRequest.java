package songoycollection.reglement.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReglementRequest(
        @NotNull(message = "Le montant du règlement est obligatoire.")
        @DecimalMin(value = "0.01", message = "Le montant du règlement doit être positif.")
        BigDecimal montant,

        @NotNull(message = "Le mode de paiement est obligatoire.")
        EnumShared.ModePaiement modePaiement,

        @NotNull(message = "La date du règlement est obligatoire.")
        LocalDateTime date
) {
}
