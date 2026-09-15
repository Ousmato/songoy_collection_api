package songoycollection.achat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDateTime;

@Entity
@Data
public class Achat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    @Column(unique = true)
    private String reference;

    private String numeroFacture;

    @Enumerated(EnumType.STRING)
    private EnumShared.Entite entite;

    @Enumerated(EnumType.STRING)
    private EnumShared.StatutAchat statut = EnumShared.StatutAchat.BROUILLON;

    private LocalDateTime dateValidation;

    @NotNull(message = "La personne responsable est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel adminResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    private Fournisseur fournisseur;

    private String nomFournisseur;

    @Enumerated(EnumType.STRING)
    private EnumShared.ModePaiement modePaiement;

}
