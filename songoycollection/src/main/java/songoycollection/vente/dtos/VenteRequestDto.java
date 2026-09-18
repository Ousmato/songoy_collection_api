package songoycollection.vente.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;
import java.util.List;

@Data
public class VenteRequestDto {

    @NotNull(message = "La date de vente est obligatoire.")
    private LocalDate date;
    private Long clientId;
    private String clientNom;
    @NotNull(message = "Le mode de paiement est obligatoire.")
    private EnumShared.ModePaiement modePaiement;

    /**
     * Montant réellement versé lors de la vente.
     * S'il est absent, le mode CREDIT vaut zéro et les autres modes valent le total.
     */
    @PositiveOrZero(message = "Le montant payé ne peut pas être négatif.")
    private Double montantPaye;

    @NotEmpty(message = "La vente doit contenir au moins une ligne.")
    private List<@NotNull @Valid VenteLigneRequestDto> lignes;
}
