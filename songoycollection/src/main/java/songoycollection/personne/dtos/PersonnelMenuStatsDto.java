package songoycollection.personne.dtos;

import lombok.Data;

/** Compteurs affiches dans le menu de gestion du personnel. */
@Data
public class PersonnelMenuStatsDto {
    private long nombreClients;
    private long nombreFournisseurs;
    private long nombrePersonnel;
}
