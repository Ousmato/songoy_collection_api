package songoycollection.commande.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.personne.model.Personnel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel client;

    private String nomClient;

    @NotNull(message = "Personnel responsable obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel admin;

    private LocalDate dateLivraison;

    @NotNull(message = "La date d'échéance est obligatoire.")
    private LocalDate dateEcheance;
}
