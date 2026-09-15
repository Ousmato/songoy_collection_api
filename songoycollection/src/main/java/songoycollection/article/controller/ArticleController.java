package songoycollection.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.article.dtos.ArticleRequestDto;
import songoycollection.article.dtos.ArticleTypeRequestDto;
import songoycollection.article.dtos.ArticleVariantRequestDto;
import songoycollection.article.dtos.SimpleArticleResponse;
import songoycollection.shared.ServiceShared;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class ArticleController {
    private final ServiceShared serviceShared;
    
    @PostMapping("add-article-type/{idAdmin}")
    @Operation(summary = "Ajouter un article type")
    public Object createArticle(@PathVariable Long idAdmin,
                                @RequestBody ArticleTypeRequestDto requestDto) {
        return serviceShared.getArticleService().addArticleType(requestDto, idAdmin);
    }

    @GetMapping("get-all-article-type/{idAdmin}")
    @Operation(summary = "Recuperer les types d'articles")
    public Object getAllArticleType(@PathVariable Long idAdmin) {
        return serviceShared.getArticleService().getAllArticleTypes(idAdmin);
    }

    @PostMapping("add-article/{idAdmin}")
    @Operation(summary = "Ajouter un article")
    public Object ajoutArticle(
            @ModelAttribute ArticleRequestDto dto,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService().addArticle(dto, idAdmin);
    }

    @PostMapping("add-article-variant/{articleId}/{idAdmin}")
    @Operation(summary = "Ajouter une variante à un article")
    public Object addArticleVariant(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin,
            @RequestBody ArticleVariantRequestDto request
    ) {
        return serviceShared.getArticleService()
                .addArticleVariant(articleId, request, idAdmin);
    }

    @GetMapping("get-article-context/{articleId}/{idAdmin}")
    @Operation(summary = "Récupérer les informations nécessaires aux variantes")
    public Object getArticleContext(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService().getArticleContext(articleId, idAdmin);
    }

    @GetMapping("get-all-articles/{idAdmin}")
    @Operation(summary = "Récupérer tous les articles")
    public List<SimpleArticleResponse> getAllArticles(@PathVariable Long idAdmin) {
        return serviceShared.getArticleService().getAllArticles(idAdmin);
    }

    @GetMapping("get-articles-by-category/{categoryId}/{idAdmin}")
    @Operation(summary = "Récupérer les articles d'une catégorie")
    public List<SimpleArticleResponse> getArticlesByCategory(
            @PathVariable Long categoryId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService()
                .getArticlesByCategory(categoryId, idAdmin);
    }

    @GetMapping("get-article-variants/{articleId}/{idAdmin}")
    @Operation(summary = "Récupérer les variantes d'un article")
    public Object getArticleVariants(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService()
                .getArticleVariants(articleId, idAdmin);
    }

    @PutMapping("update-article-variant/{variantId}/{idAdmin}")
    @Operation(summary = "Modifier les caractéristiques d'une variante")
    public Object updateArticleVariant(
            @PathVariable Long variantId,
            @PathVariable Long idAdmin,
            @RequestBody ArticleVariantRequestDto request
    ) {
        return serviceShared.getArticleService()
                .updateArticleVariant(variantId, request, idAdmin);
    }
}
