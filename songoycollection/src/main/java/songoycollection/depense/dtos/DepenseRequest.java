package songoycollection.depense.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Données nécessaires à l'enregistrement d'une charge générale. */
public record DepenseRequest(
        @NotNull(message = "La date de la dépense est obligatoire.")
        @PastOrPresent(message = "La date de la dépense ne peut pas être future.")
        LocalDate date,

        @NotNull(message = "Le motif de la dépense est obligatoire.")
        EnumShared.MotifDepense motif,

        @NotNull(message = "L'entité de la dépense est obligatoire.")
        EnumShared.Entite entite,

        @NotNull(message = "Le montant de la dépense est obligatoire.")
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
        @Digits(integer = 15, fraction = 2, message = "Le montant doit contenir au maximum deux décimales.")
        BigDecimal montant,

        @NotNull(message = "Le moyen de paiement est obligatoire.")
        EnumShared.ModePaiement modePaiement,

        /** Obligatoire lorsque le motif est SALAIRE, au format AAAA-MM. */
        String periodeSalaire,

        Long personnelBeneficiaireId,

        Long fournisseurId,

        String nomFournisseur,

        @NotNull(message = "La description est obligatoire.")
        String libelle
) {
}
