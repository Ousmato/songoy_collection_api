package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;

/** Informations d'un membre de l'equipe, sans donnees de connexion. */
@Data
public class SimplePersonnelResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String accessCode;
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;
    private EnumShared.PersonnelRole role;
    private EnumShared.Entite entite;

    public static SimplePersonnelResponseDto fromPersonnel(Personnel personnel) {
        SimplePersonnelResponseDto dto = new SimplePersonnelResponseDto();
        dto.setId(personnel.getId());
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        dto.setAdresse(personnel.getAdresse());
        dto.setTelephone(personnel.getTelephone());
        dto.setDateNaissance(personnel.getDateNaissance());
        dto.setRole(personnel.getRole());
        dto.setEntite(personnel.getEntite());
        dto.setAccessCode(personnel.getAccessCode());
        return dto;
    }
}
