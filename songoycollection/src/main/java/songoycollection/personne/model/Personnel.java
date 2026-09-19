package songoycollection.personne.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;

@Entity
@Data
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Le nom est obligatoire.")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire.")
    private String prenom;

    @NotBlank(message = "L'adresse est obligatoire.")
    private String adresse;

    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    @Pattern(regexp = "^[0-9]{8}$", message = "Le numéro de téléphone doit contenir exactement 8 chiffres.")
    private String telephone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(unique = true, updatable = false)
    private String accessCode;

    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    private EnumShared.PersonnelRole role;

    @Enumerated(EnumType.STRING)
    private EnumShared.Entite entite;

    private boolean active = true;

    private LocalDate dateAjout;


    @PrePersist
    public void prePersist(){
        dateAjout = LocalDate.now();

    }
}
