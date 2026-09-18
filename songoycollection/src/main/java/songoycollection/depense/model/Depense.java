package songoycollection.depense.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.personne.model.Fournisseur;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnumShared.MotifDepense motifDepense;

    @Enumerated(EnumType.STRING)
    private EnumShared.Entite entite;

    private LocalDateTime date;

    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private EnumShared.ModePaiement modePaiement;

    @NotNull(message = "Administrateur obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel personnelResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel personnelBeneficiaire;

    @ManyToOne(fetch = FetchType.LAZY)
    private Fournisseur fournisseur;

    private String nomFournisseur;

    @NotBlank(message = "La description est obligatoire.")
    private String libelle;



}
