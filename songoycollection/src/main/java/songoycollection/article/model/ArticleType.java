package songoycollection.article.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class ArticleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Le nom du type article obligatoire")
    private String nom;

    @NotBlank(message = "Description obligatoire")
    private String description;

}
