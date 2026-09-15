package songoycollection.article.dtos;

import lombok.Data;
import songoycollection.article.model.ArticleType;

@Data
public class ArticleTypeRequestDto {
    private Long idArticle;
    private String nom;
    private String description;

    public static ArticleType toArticleType(ArticleTypeRequestDto articleTypeRequestDto) {
        ArticleType articleType = new ArticleType();
        articleType.setId(articleTypeRequestDto.getIdArticle());
        articleType.setNom(articleTypeRequestDto.getNom());
        articleType.setDescription(articleTypeRequestDto.getDescription());
        return articleType;
    }
}
