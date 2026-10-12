package songoycollection.atelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.atelier.model.enums.TypeMouvementMatiereAtelier;
import songoycollection.personne.model.Personnel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class MouvementStockMatiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le stock de matiere est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_matiere_atelier_id", nullable = false)
    private StockMatiereAtelier stockMatiere;

    @NotNull(message = "Le type de mouvement est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvementMatiereAtelier type;

    @NotNull(message = "La quantite est obligatoire.")
    @DecimalMin(value = "0.001", message = "La quantite doit etre positive.")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantite;

    @NotNull(message = "La date du mouvement est obligatoire.")
    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @NotNull(message = "Le personnel est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @Column(length = 500)
    private String observation;

    @jakarta.persistence.PrePersist
    private void prePersist() {
        if (dateMouvement == null) {
            dateMouvement = LocalDateTime.now();
        }
    }
}
