package songoycollection.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping(
            value = "add-article-variant/{articleId}/{idAdmin}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Ajouter une variante, avec image facultative")
    public Object addArticleVariant(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin,
            @RequestPart("request") ArticleVariantRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return serviceShared.getArticleService()
                .addArticleVariant(articleId, request, image, idAdmin);
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

    @PutMapping(
            value = "update-article-variant/{variantId}/{idAdmin}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Modifier une variante, avec image facultative")
    public Object updateArticleVariant(
            @PathVariable Long variantId,
            @PathVariable Long idAdmin,
            @RequestPart("request") ArticleVariantRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return serviceShared.getArticleService()
                .updateArticleVariant(variantId, request, image, idAdmin);
    }
}
