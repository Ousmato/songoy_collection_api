package songoycollection.article.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import songoycollection.article.model.ModeleArticle;

@Data
@AllArgsConstructor
public class ModeleArticleDto {
    private Long id;
    private Long articleId;
    private String nom;
    private String marque;
    private String matiere;

    public static ModeleArticleDto from(ModeleArticle modeleArticle) {
        return new ModeleArticleDto(
                modeleArticle.getId(),
                modeleArticle.getArticle().getId(),
                modeleArticle.getNom(),
                modeleArticle.getMarque(),
                modeleArticle.getMatiere()
        );
    }
}
