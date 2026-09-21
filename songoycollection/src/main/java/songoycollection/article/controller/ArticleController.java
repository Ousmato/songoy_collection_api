package songoycollection.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import songoycollection.article.dtos.ArticleRequestDto;
import songoycollection.article.dtos.ArticleTypeRequestDto;
import songoycollection.article.dtos.ArticleVariantRequestDto;
import songoycollection.article.dtos.DeclinaisonDto;
import songoycollection.article.dtos.DeclinaisonRequestDto;
import songoycollection.article.dtos.ModeleArticleDto;
import songoycollection.article.dtos.ModeleArticleRequestDto;
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

    @PostMapping("add-modele-article/{articleId}/{idAdmin}")
    @Operation(summary = "Ajouter un modèle à un article")
    public ModeleArticleDto addModeleArticle(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin,
            @RequestBody ModeleArticleRequestDto request
    ) {
        return serviceShared.getArticleService()
                .addModeleArticle(articleId, request, idAdmin);
    }

    @PostMapping(
            value = "add-declinaison/{modeleArticleId}/{idAdmin}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Ajouter une déclinaison à un modèle d'article")
    public DeclinaisonDto addDeclinaison(
            @PathVariable Long modeleArticleId,
            @PathVariable Long idAdmin,
            @RequestPart("request") DeclinaisonRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return serviceShared.getArticleService()
                .addDeclinaison(modeleArticleId, request, image, idAdmin);
    }

    @PutMapping(
            value = "update-declinaison/{declinaisonId}/{idAdmin}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Modifier l'image ou les caractéristiques d'une déclinaison")
    public DeclinaisonDto updateDeclinaison(
            @PathVariable Long declinaisonId,
            @PathVariable Long idAdmin,
            @RequestPart(value = "request", required = false) DeclinaisonRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return serviceShared.getArticleService()
                .updateDeclinaison(declinaisonId, request, image, idAdmin);
    }

    @PostMapping("add-article-variant/{declinaisonId}/{idAdmin}")
    @Operation(summary = "Ajouter une variante à une déclinaison")
    public Object addArticleVariant(
            @PathVariable Long declinaisonId,
            @PathVariable Long idAdmin,
            @RequestBody ArticleVariantRequestDto request
    ) {
        return serviceShared.getArticleService()
                .addArticleVariant(declinaisonId, request, idAdmin);
    }

    @GetMapping("get-article-context/{articleId}/{idAdmin}")
    @Operation(summary = "Récupérer les informations nécessaires aux variantes")
    public Object getArticleContext(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService().getArticleContext(articleId, idAdmin);
    }

    @GetMapping("get-modeles-article/{articleId}/{idAdmin}")
    @Operation(summary = "Récupérer les modèles d'un article")
    public List<ModeleArticleDto> getModelesArticle(
            @PathVariable Long articleId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService().getModelesArticle(articleId, idAdmin);
    }

    @GetMapping("get-declinaisons-modele/{modeleArticleId}/{idAdmin}")
    @Operation(summary = "Récupérer les déclinaisons d'un modèle")
    public List<DeclinaisonDto> getDeclinaisonsModele(
            @PathVariable Long modeleArticleId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService()
                .getDeclinaisonsModele(modeleArticleId, idAdmin);
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

    @GetMapping("get-declinaison-variants/{declinaisonId}/{idAdmin}")
    @Operation(summary = "Récupérer les variantes d'une déclinaison")
    public Object getDeclinaisonVariants(
            @PathVariable Long declinaisonId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService()
                .getDeclinaisonVariants(declinaisonId, idAdmin);
    }

    @DeleteMapping("delete-article-variant/{variantId}/{idAdmin}")
    @Operation(summary = "Supprimer une variante sans mouvement ni historique")
    public Object deleteArticleVariant(
            @PathVariable Long variantId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getArticleService()
                .deleteArticleVariant(variantId, idAdmin);
    }

    @PutMapping(
            value = "update-article-variant/{variantId}/{idAdmin}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Modifier le prix, la référence ou les caractéristiques d'une variante")
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
