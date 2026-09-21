package songoycollection.article.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import songoycollection.article.model.Declinaison;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.text.Normalizer;

@Data
@AllArgsConstructor
public class DeclinaisonDto {
    private Long id;
    private Long modeleArticleId;
    private String urlImage;
    private String referenceCommune;
    private List<ArticleVariantDto> variants;
    private Map<Long, String> attributs;
    private boolean caracteristiquesModifiables;

    public static DeclinaisonDto from(
            Declinaison declinaison,
            Map<Long, String> attributs,
            boolean caracteristiquesModifiables
    ) {
        return new DeclinaisonDto(
                declinaison.getId(),
                declinaison.getModeleArticle().getId(),
                declinaison.getUrlImage(),
                buildReferenceCommune(declinaison, attributs),
                List.of(),
                attributs,
                caracteristiquesModifiables
        );
    }

    private static String buildReferenceCommune(Declinaison declinaison, Map<Long, String> attributs) {
        List<String> parts = new ArrayList<>();
        var modele = declinaison.getModeleArticle();
        var article = modele.getArticle();
        addReferencePart(parts, article.getCategory().getNom());
        addReferencePart(parts, article.getArticleType().getNom());
        addReferencePart(parts, modele.getMarque());
        addReferencePart(parts, modele.getNom());
        addReferencePart(parts, modele.getMatiere());
        (attributs == null ? List.<String>of() : attributs.values())
                .forEach(value -> addReferencePart(parts, value));
        return String.join("-", parts);
    }

    private static void addReferencePart(List<String> parts, String value) {
        if (value == null || value.isBlank()) return;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (!normalized.isBlank()) parts.add(normalized);
    }
}
