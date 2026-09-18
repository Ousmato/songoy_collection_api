package songoycollection.vente.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VenteLigneRequestDto {
    @NotNull(message = "La variante est obligatoire.")
    @Positive(message = "La variante est invalide.")
    private Long variantId;

    @NotNull(message = "La quantité est obligatoire.")
    @Positive(message = "La quantité doit être positive.")
    private Double quantite;

    @NotNull(message = "Le prix de vente est obligatoire.")
    @Positive(message = "Le prix de vente doit être positif.")
    private Integer prixVente;

}
