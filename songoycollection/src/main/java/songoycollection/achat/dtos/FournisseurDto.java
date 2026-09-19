package songoycollection.achat.dtos;

import lombok.Data;

/** Coordonnées du fournisseur et identité minimale de son responsable. */
@Data
public class FournisseurDto {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private PersonnelAchatDto adminResponsable;
}
