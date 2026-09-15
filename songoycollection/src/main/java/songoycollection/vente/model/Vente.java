package songoycollection.vente.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.commande.model.Commande;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.time.LocalDateTime;

@Entity
@Data
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    @NotNull(message = "Personnel encaissé obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel caissier;

    private String nomClient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel client;

    @ManyToOne(fetch = FetchType.LAZY)
    private Commande commande;

    @NotNull(message = "Mode de paiement est obligatoire.")
    @Enumerated(EnumType.STRING)
    private EnumShared.ModePaiement modePaiement;
}
