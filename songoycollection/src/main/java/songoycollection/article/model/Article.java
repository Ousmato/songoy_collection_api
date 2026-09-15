package songoycollection.article.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.category.model.Category;

import java.time.LocalDateTime;

@Entity
@Data
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Catégorie obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private boolean active = true;

    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private ArticleType articleType;


}
