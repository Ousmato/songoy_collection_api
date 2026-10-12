package songoycollection.atelier.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepotMatiereClientRequestDto {

    @NotBlank(message = "La description de la matière est obligatoire.")
    private String description;

    @NotNull(message = "La quantité reçue est obligatoire.")
    @DecimalMin(value = "0.001", message = "La quantité reçue doit être positive.")
    private BigDecimal quantite;

    @NotBlank(message = "L’unité de la matière est obligatoire.")
    private String unite;

    /** Clé de la part multipart contenant la photo de la matière, si elle existe. */
    private String photoReferenceKey;
}
