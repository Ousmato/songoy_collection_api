package songoycollection.depense.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import songoycollection.shared.EnumShared;
import java.time.LocalDate;
import java.util.List;

public record ReceptionRequest(
        @NotNull LocalDate date,
        EnumShared.Entite entite,
        Long fournisseurId,
        @Size(max = 255) String nomFournisseur,
        @Size(max = 255) String numeroFacture,
        @NotEmpty List<@NotNull @Valid Ligne> lignes,
        @Valid Paiement paiement
) {
    public record Ligne(
            @NotNull @Positive Long variantId,
            @NotNull @Positive Double quantite,
            @NotNull @Positive Double prixAchat
    ) {}

    public record Paiement(
            @NotNull @PositiveOrZero Double montant,
            @NotNull EnumShared.ModePaiement modePaiement
    ) {}
}
