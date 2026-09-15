package songoycollection.stock.model;

import jakarta.persistence.*;
import lombok.Data;
import songoycollection.article.model.ArticleVariant;
import songoycollection.shared.EnumShared;
import songoycollection.vente.model.LigneVente;

import java.time.LocalDateTime;

@Entity
@Data
public class StockMouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockLot stockLot;

    @ManyToOne(fetch = FetchType.LAZY)
    private ArticleVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    private LigneVente ligneVente;

    private Double quantite;

    @Enumerated(EnumType.STRING)
    private EnumShared.TypeMouvement type;

    private String reference;

    private LocalDateTime dateEntree ;
}
