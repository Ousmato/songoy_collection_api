package songoycollection.atelier.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeConfectionRequestDto {

    @Positive(message = "Le client sélectionné est invalide.")
    private Long clientId;

    @Size(max = 120, message = "Le nom du client de passage est trop long.")
    private String clientNomPassage;

    @NotNull(message = "La date de livraison prévue est obligatoire.")
    private LocalDate dateLivraisonPrevue;

    private String observation;

    @Valid
    private VersementInitialCommandeRequestDto versementInitial;

    @NotEmpty(message = "La commande doit contenir au moins un ouvrage.")
    private List<@NotNull @Valid OuvrageConfectionRequestDto> ouvrages;

    @AssertTrue(message = "Indiquez soit un client enregistré, soit le nom d’un client de passage.")
    public boolean isClientCoherent() {
        boolean hasClientEnregistre = clientId != null;
        boolean hasClientPassage = clientNomPassage != null && !clientNomPassage.isBlank();
        return hasClientEnregistre != hasClientPassage;
    }
}
