package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;

/**
 * Informations affichables du compte connecté.
 * Les données de connexion (mot de passe et code d'accès) n'y figurent jamais.
 */
@Data
public class PersonnelProfileResponseDto {
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;
    private EnumShared.PersonnelRole role;
    private EnumShared.Entite entite;

    public static PersonnelProfileResponseDto fromPersonnel(Personnel personnel) {
        PersonnelProfileResponseDto dto = new PersonnelProfileResponseDto();
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        dto.setAdresse(personnel.getAdresse());
        dto.setTelephone(personnel.getTelephone());
        dto.setDateNaissance(personnel.getDateNaissance());
        dto.setRole(personnel.getRole());
        dto.setEntite(personnel.getEntite());
        return dto;
    }
}
