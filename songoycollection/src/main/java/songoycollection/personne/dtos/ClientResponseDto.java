package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;

/** Données affichables d'un client, sans information d'authentification. */
@Data
public class ClientResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private EnumShared.Entite entite;
    private LocalDate dateAjout;

    public static ClientResponseDto fromPersonnel(Personnel client) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setAdresse(client.getAdresse());
        dto.setTelephone(client.getTelephone());
        dto.setEntite(client.getEntite());
        dto.setDateAjout(client.getDateAjout());
        return dto;
    }
}
