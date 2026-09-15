package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;
@Data
public class PersonnelDto {
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private String password;

    private String accessCode;

    private LocalDate dateNaissance;

    private EnumShared.PersonnelRole role;

    private EnumShared.Entite entite;

    public static PersonnelDto fromPersonnel(Personnel personnel) {
        PersonnelDto dto = new PersonnelDto();
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        dto.setAdresse(personnel.getAdresse());
        dto.setTelephone(personnel.getTelephone());
        dto.setAccessCode(personnel.getAccessCode());
        dto.setDateNaissance(personnel.getDateNaissance());
        dto.setRole(personnel.getRole());
        dto.setEntite(personnel.getEntite());
        return dto;
    }

    public static Personnel toPersonnel(PersonnelDto dto) {
        Personnel personnel = new Personnel();
        personnel.setNom(dto.getNom());
        personnel.setPrenom(dto.getPrenom());
        personnel.setAdresse(dto.getAdresse());
        personnel.setPassword(dto.getPassword());
        personnel.setTelephone(dto.getTelephone());
        personnel.setAccessCode(dto.getAccessCode());
        personnel.setDateNaissance(dto.getDateNaissance());
        personnel.setRole(dto.getRole());
        personnel.setEntite(dto.getEntite());
        return personnel;
    }
}
