package songoycollection.article.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class DeclinaisonRequestDto {
    private Map<Long, String> attributs;
}
