package songoycollection.personne.dtos;

import lombok.Data;
import songoycollection.personne.model.Fournisseur;

@Data
public class FournisseurRequestDto {
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;


    public static Fournisseur toFournisseur(FournisseurRequestDto requestDto) {
        Fournisseur f = new Fournisseur();
        f.setNom(requestDto.getNom());
        f.setPrenom(requestDto.getPrenom());
        f.setTelephone(requestDto.getTelephone());
        f.setAdresse(requestDto.getAdresse());
        return f;
    }

}
