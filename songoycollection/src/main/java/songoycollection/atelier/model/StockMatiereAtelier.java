package songoycollection.atelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.article.model.ArticleVariant;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;

@Data
@Entity
public class StockMatiereAtelier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La variante identifie la matiere. Elle ne represente pas un stock de vente boutique.
     */
    @NotNull(message = "La variante de la matiere est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ArticleVariant variant;

    @NotNull(message = "L'unite de mesure est obligatoire.")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private EnumShared.UniteMesureCategory unite;

    @NotNull(message = "La quantite disponible est obligatoire.")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantiteDisponible = BigDecimal.ZERO;
}
