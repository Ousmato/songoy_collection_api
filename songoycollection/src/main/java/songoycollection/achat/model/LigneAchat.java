package songoycollection.achat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.article.model.ArticleVariant;

import java.time.LocalDateTime;

@Entity
@Data
public class LigneAchat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La variante de l'article est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY)
    private ArticleVariant variant;

    // Quantité effectivement reçue pour cette variante.
    private Double quantite = 0.0;

    // Prix d'achat unitaire ; le montant de ligne sera quantité × prixAchat.
    private Double prixAchat;

    private LocalDateTime date;

    private boolean annulee = false;

    private LocalDateTime dateAnnulation;

    @NotNull(message = "Achat invalide, verifier les champs")
    @ManyToOne(fetch = FetchType.LAZY)
    private Achat achat;


}
