package songoycollection.article.dtos;

import lombok.Data;
import songoycollection.category.dtos.AttributeDto;

import java.util.List;

@Data
public class ArticleContextDto {
    private Long id;
    private Long categoryId;
    private String categoryNom;
    private String typeNom;
    private List<AttributeDto> attributes;
}
