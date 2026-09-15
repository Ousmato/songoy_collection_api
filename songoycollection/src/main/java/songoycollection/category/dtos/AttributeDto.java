package songoycollection.category.dtos;

import lombok.Data;
import songoycollection.category.model.CategoryAttribute;

@Data
public class AttributeDto {
    private Long id;
    private String key;
    private String label;
    private String type;
    private boolean obligatoire;
    private Integer ordre;

    public static AttributeDto createCategoryAttributeDto(CategoryAttribute cta) {
        AttributeDto dto = new AttributeDto();
        dto.setId(cta.getAttribute().getId());
        dto.setKey(cta.getAttribute().getKey());
        dto.setObligatoire(cta.isObligatoire());
        dto.setOrdre(cta.getOrdre());
        dto.setType(cta.getAttribute().getType());
        dto.setLabel(cta.getAttribute().getLabel());
        return dto;
    }
}
