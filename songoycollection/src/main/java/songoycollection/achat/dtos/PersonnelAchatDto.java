package songoycollection.achat.dtos;

import lombok.Data;

/** Responsable ayant enregistré la réception. */
@Data
public class PersonnelAchatDto {
    private Long id;
    private String nom;
    private String prenom;
}
