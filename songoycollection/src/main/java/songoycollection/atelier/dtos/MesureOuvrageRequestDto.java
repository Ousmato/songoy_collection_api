package songoycollection.atelier.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MesureOuvrageRequestDto {

    @NotBlank(message = "Le libellé de la mesure est obligatoire.")
    private String libelle;

    @NotNull(message = "La valeur de la mesure est obligatoire.")
    @DecimalMin(value = "0", message = "La valeur de la mesure ne peut pas être négative.")
    private BigDecimal valeur;

    @NotBlank(message = "L’unité de mesure est obligatoire.")
    private String unite;
}
