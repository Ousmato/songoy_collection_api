package songoycollection.depense.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;
import songoycollection.vente.model.Vente;

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

    private Double montant;

    @Enumerated(EnumType.STRING)
    private EnumShared.ModePaiement modePaiement;

    @NotNull(message = "Administrateur obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel personnel;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vente vente;



}
