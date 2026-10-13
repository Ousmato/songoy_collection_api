package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

@Data
public class LoginResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String accessCode;
    private EnumShared.PersonnelRole role;
    private EnumShared.Entite entite;
    private String accessToken;
    private String tokenType;
    private long expiresIn;

    public static LoginResponseDto fromPersonnel(Personnel personnel) {
        LoginResponseDto response = new LoginResponseDto();
        response.setId(personnel.getId());
        response.setNom(personnel.getNom());
        response.setPrenom(personnel.getPrenom());
        response.setAccessCode(personnel.getAccessCode());
        response.setRole(personnel.getRole());
        response.setEntite(personnel.getEntite());
        return response;
    }
}
