package songoycollection.atelier.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;

@Data
public class OuvrageEtatUpdateRequestDto {

    @NotNull(message = "Le nouvel état de l'ouvrage est obligatoire.")
    private EtatOuvrageConfection etat;

    @DecimalMin(value = "0", message = "Le montant du règlement ne peut pas être négatif.")
    private BigDecimal montantReglement = BigDecimal.ZERO;

    private EnumShared.ModePaiement modePaiement;
}
