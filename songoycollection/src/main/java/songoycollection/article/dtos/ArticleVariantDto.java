package songoycollection.article.dtos;

import lombok.Data;
import songoycollection.article.model.ArticleVariant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ArticleVariantDto {
    private Long id;
    private String reference;
    private Integer prixVente;
    private String urlImage;
    private Double quantity = 0.0;
    private Map<Long, String> attributs;

    public static ArticleVariantDto from(ArticleVariant variant) {
        ArticleVariantDto dto = new ArticleVariantDto();
        dto.setId(variant.getId());
        dto.setReference(variant.getReference());
        dto.setPrixVente(variant.getPrixVente());
        dto.setUrlImage(fileName(variant.getUrlImage()));
        Map<Long, String> values = new LinkedHashMap<>();
        variant.getAttributs().forEach(attribute ->
                values.put(attribute.getAttribute().getId(), attribute.getValue())
        );
        dto.setAttributs(values);
        return dto;
    }

    public static List<ArticleVariantDto> from(List<ArticleVariant> variants) {
        return variants.stream().map(ArticleVariantDto::from).toList();
    }

    private static String fileName(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return storedPath;
        }
        String normalized = storedPath.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        return separator >= 0 ? normalized.substring(separator + 1) : normalized;
    }
}
