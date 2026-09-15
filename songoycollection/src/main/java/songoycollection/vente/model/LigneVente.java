package songoycollection.vente.model;

import jakarta.persistence.*;
import lombok.Data;
import songoycollection.article.model.ArticleVariant;

import java.time.LocalDateTime;

@Entity
@Data
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double quantite = 0.0;

    private Integer prixVente;

    private LocalDateTime date;

    private boolean annulee = false;

    private LocalDateTime dateAnnulation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ArticleVariant variant;
}
