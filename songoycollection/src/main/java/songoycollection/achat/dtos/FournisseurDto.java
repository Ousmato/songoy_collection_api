package songoycollection.achat.dtos;

import lombok.Data;

/** Coordonnées minimales du fournisseur liées à l'achat. */
@Data
public class FournisseurDto {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
}
