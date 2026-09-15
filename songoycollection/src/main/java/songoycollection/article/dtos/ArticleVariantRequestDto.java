package songoycollection.article.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class ArticleVariantRequestDto {
    private String reference;
    private Integer prixVente;
    private Map<Long, String> attributs;
}
