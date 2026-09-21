package songoycollection.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.category.dtos.CategoryAttributeDto;
import songoycollection.category.dtos.CategoryResponseDto;
import songoycollection.category.dtos.CategoryAttributeRequestDto;
import songoycollection.category.dtos.CategoryNameUpdateRequestDto;
import songoycollection.category.model.CategoryAttribute;
import songoycollection.category.model.Category;
import songoycollection.shared.ServiceShared;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class CategoryController {

    private final ServiceShared serviceShared;

    @GetMapping("get-all-category/{idAdmin}")
    @Operation(summary = "Recuperer tout les categories")
    public List<CategoryResponseDto> getAllCategories(@PathVariable Long idAdmin) {
        return serviceShared.getCategoryService().getAllCategories(idAdmin);
    }

    @PostMapping("add-categorie/{idAdmin}")
    @Operation(summary = "Ajouter categorie")
    public Object addCategorie(@PathVariable Long idAdmin, @RequestBody Category category) {
        return serviceShared.getCategoryService().addCategory(category, idAdmin);
    }

    @PutMapping("update-category-name/{categoryId}/{idAdmin}")
    @Operation(summary = "Modifier le nom d'une categorie")
    public Object updateCategoryName(
            @PathVariable Long categoryId,
            @PathVariable Long idAdmin,
            @RequestBody CategoryNameUpdateRequestDto request
    ) {
        return serviceShared.getCategoryService()
                .updateCategoryName(categoryId, request, idAdmin);
    }

    @DeleteMapping("delete-category/{categoryId}/{idAdmin}")
    @Operation(summary = "Supprimer une categorie sans article associe")
    public Object deleteCategory(
            @PathVariable Long categoryId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getCategoryService().deleteCategory(categoryId, idAdmin);
    }

    @PostMapping("add-category-attribute/{idAdmin}")
    @Operation(summary = "Ajouter un attribut à plusieurs catégories")
    public Object addCategoryAttributes(
            @PathVariable Long idAdmin,
            @RequestBody CategoryAttributeRequestDto request
    ) {
        return serviceShared.getCategoryService()
                .addCategoryAttributes(request, idAdmin);
    }

    @GetMapping("get-category-attributes/{categoryId}/{idAdmin}")
    @Operation(summary = "Récupérer les attributs d'une catégorie")
    public List<CategoryAttributeDto> getCategoryAttributes(
            @PathVariable Long categoryId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getCategoryService()
                .getCategoryAttributes(categoryId, idAdmin);
    }

    @GetMapping("get-all-category-attributes/{idAdmin}")
    @Operation(summary = "Récupérer les catégories avec leurs caractéristiques")
    public List<CategoryAttributeDto> getAllCategoryAttributes(
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getCategoryService().getAllCategoryAttributes(idAdmin);
    }
}
