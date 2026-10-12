package songoycollection.atelier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class DepotMatiereClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ouvrage est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OuvrageConfection ouvrage;

    @NotBlank(message = "La description de la matiere est obligatoire.")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "La quantite recue est obligatoire.")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantiteRecue;

    @NotNull(message = "La quantite consommee est obligatoire.")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantiteConsommee = BigDecimal.ZERO;

    @NotNull(message = "La quantite retournee est obligatoire.")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantiteRetournee = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String unite;

    @Column(length = 500)
    private String urlPhotoReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateDepot;

    private LocalDateTime dateRetour;

    @Column(length = 500)
    private String observation;

    @PrePersist
    private void prePersist() {
        if (dateDepot == null) {
            dateDepot = LocalDateTime.now();
        }
    }
}
