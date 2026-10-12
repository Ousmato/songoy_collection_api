package songoycollection.atelier.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MesureOuvrageDto {
    private Long id;
    private String libelle;
    private BigDecimal valeur;
    private String unite;
}
