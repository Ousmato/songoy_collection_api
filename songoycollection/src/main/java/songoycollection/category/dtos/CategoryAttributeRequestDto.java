package songoycollection.category.dtos;

import lombok.Data;
import songoycollection.category.model.CategoryAttributeLevel;

import java.util.List;

@Data
public class CategoryAttributeRequestDto {
    private List<Long> categoryIds;
    private String key;
    private String label;
    private String type;
    private boolean obligatoire;
    private Integer ordre;
    private CategoryAttributeLevel niveau;
}
