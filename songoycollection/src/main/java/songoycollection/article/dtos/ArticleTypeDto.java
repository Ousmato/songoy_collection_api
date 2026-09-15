package songoycollection.article.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import songoycollection.article.model.ArticleType;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleTypeDto extends ArticleType {

    public static ArticleTypeDto getArticleTypeDto(ArticleType articleType) {
        ArticleTypeDto dto = new ArticleTypeDto();
        dto.setId(articleType.getId());
        dto.setNom(articleType.getNom());
        dto.setDescription(articleType.getDescription());
        return dto;
    }

    public static List<ArticleTypeDto> getArticleTypeDtos(List<ArticleType> articleTypes) {
        List<ArticleTypeDto> dtos = new ArrayList<>();
        for (ArticleType articleType : articleTypes) {
            dtos.add(getArticleTypeDto(articleType));
        }
        return dtos;
    }
}
