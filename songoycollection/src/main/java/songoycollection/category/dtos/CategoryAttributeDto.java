package songoycollection.category.dtos;

import lombok.Data;
import songoycollection.category.model.CategoryAttribute;

import java.util.List;

@Data
public class CategoryAttributeDto {
    private String categoryNom;
    private Long categoryId;
    private List<AttributeDto> attributes;

    public static CategoryAttributeDto createCategoryAttributeDto(
            Long categoryId,
            String categoryNom,
            List<CategoryAttribute> attributes
    ) {
        CategoryAttributeDto dto = new CategoryAttributeDto();
        dto.setCategoryId(categoryId);
        dto.setCategoryNom(categoryNom);
        dto.setAttributes(attributes.stream()
                .map(AttributeDto::createCategoryAttributeDto)
                .toList());
        return dto;
    }
}
