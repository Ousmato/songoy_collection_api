package songoycollection.atelier.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;

@Data
public class VersementInitialCommandeRequestDto {

    @NotNull(message = "Le montant du versement est obligatoire.")
    @DecimalMin(value = "1", message = "Le versement initial doit être positif.")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire.")
    private EnumShared.ModePaiement modePaiement;
}
