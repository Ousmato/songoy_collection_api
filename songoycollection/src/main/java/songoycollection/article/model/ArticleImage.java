package songoycollection.article.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ArticleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String urlImage;

    @ManyToOne(fetch = FetchType.LAZY)
    private Article article;
}
