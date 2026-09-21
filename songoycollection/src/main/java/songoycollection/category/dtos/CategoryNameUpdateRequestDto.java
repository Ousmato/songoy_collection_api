package songoycollection.category.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryNameUpdateRequestDto {

    @NotBlank(message = "Le nom de la categorie est obligatoire.")
    @Size(max = 255, message = "Le nom de la categorie ne peut pas depasser 255 caracteres.")
    private String nom;
}
