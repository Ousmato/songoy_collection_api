package songoycollection.atelier.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OuvrageConfectionRequestDto {

    @NotBlank(message = "La désignation de l’ouvrage est obligatoire.")
    private String designation;

    @NotBlank(message = "Le type d’habit est obligatoire.")
    private String typeHabit;

    @NotNull(message = "Le couturier est obligatoire.")
    @Positive(message = "Le couturier sélectionné est invalide.")
    private Long couturierId;

    @NotNull(message = "Le prix convenu est obligatoire.")
    @DecimalMin(value = "1", message = "Le prix convenu doit être positif.")
    private BigDecimal prixConvenu;

    private String consignes;

    /** Clé de la part multipart contenant la photo de référence, si elle existe. */
    private String photoReferenceKey;

    @NotEmpty(message = "L’ouvrage doit contenir au moins une mesure.")
    private List<@NotNull @Valid MesureOuvrageRequestDto> mesures;

    private List<@NotNull @Valid DepotMatiereClientRequestDto> depotsMatiereClient;
}
