package songoycollection.atelier.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AtelierMenuStatsDto {
    private long ouvragesEnConfection;
    private long ouvragesPretsALivrer;
    private long ouvragesLivresCeMois;
    private BigDecimal restesAEncaisser = BigDecimal.ZERO;
}
