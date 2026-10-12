package songoycollection.atelier.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepotMatiereDetailDto {
    private Long id;
    private String description;
    private BigDecimal quantiteRecue;
    private BigDecimal quantiteConsommee;
    private BigDecimal quantiteRetournee;
    private String unite;
    private String urlPhotoReference;
    private LocalDateTime dateDepot;
    private LocalDateTime dateRetour;
    private String observation;
}
