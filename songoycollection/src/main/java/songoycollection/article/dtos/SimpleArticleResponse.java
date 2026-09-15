package songoycollection.article.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import songoycollection.shared.EnumShared;

@Data
@AllArgsConstructor
public class SimpleArticleResponse {
    private Long id;
    private String nom;
    private Integer prixVente;
    private Long idType;
    private String typeNom;
    private Double quantity;
    private EnumShared.UniteMesureCategory categoryMesure;
}
