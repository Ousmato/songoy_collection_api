package songoycollection.personne.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Entity
public class Fournisseur {

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
}
