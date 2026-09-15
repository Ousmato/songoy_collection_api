package songoycollection.stock.model;

import jakarta.persistence.*;
import lombok.Data;
import songoycollection.article.model.ArticleVariant;
import songoycollection.achat.model.LigneAchat;
import songoycollection.personne.model.Personnel;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class StockLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double initialQuantity = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    private ArticleVariant variant;

    // Absent pour un stock initial ; référence la réception pour un achat.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_achat_id")
    private LigneAchat ligneAchat;

    private Double quantityRestante;

    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel personnel;

    private LocalDate entryDate;

    private LocalDateTime createdAt;

    @Column(length = 500)
    private String observation;

}
