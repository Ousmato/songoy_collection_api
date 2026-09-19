package songoycollection.article.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ArticleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Article obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Article article;

    private String urlImage;

    @Column(unique = true)
    private String reference;

    private Integer prixVente;

    @OneToMany(
            mappedBy = "variant",
            cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ArticleVariantAttribute> attributs = new ArrayList<>();
}
