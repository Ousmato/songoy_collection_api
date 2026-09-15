package songoycollection.personne.dtos;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String accessCode;
    private String password;
}
