package songoycollection.category.dtos;

import lombok.Data;
import songoycollection.category.model.Category;
import songoycollection.shared.EnumShared;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryResponseDto {

    private Long id;
    private String nom;
    private EnumShared.UniteMesureCategory mesureCategory;
    private EnumShared.CategoryType type;
    private EnumShared.Entite entite;
    private EnumShared.CategoryType categoryType;
    private String description;
    private Long nombreArticle;
    private boolean active;

    public static CategoryResponseDto fromCategory(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setNom(category.getNom());
        dto.setDescription(category.getDescription());
        dto.setMesureCategory(category.getMesureCategory());
        dto.setType(category.getCategoryType());
        dto.setEntite(category.getEntite());
        dto.setActive(category.isActive());
        dto.setEntite(category.getEntite());
        dto.setCategoryType(category.getCategoryType());
        return dto;
    }

    public static List<CategoryResponseDto> fromCategoryList(List<Category> categories) {
        List<CategoryResponseDto> dtos = new ArrayList<>();
        for (Category category : categories) {
            dtos.add(fromCategory(category));
        }
        return dtos;
    }
}
