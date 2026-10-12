package songoycollection.atelier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.personne.model.Personnel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class CommandeConfection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Personnel client;

    @Column(length = 120)
    private String clientNomPassage;

    @NotNull(message = "Le createur de la commande est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private Personnel creePar;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDate dateLivraisonPrevue;

    @Column(length = 500)
    private String observation;

    @PrePersist
    private void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
