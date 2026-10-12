package songoycollection.atelier.dtos;

import lombok.Data;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OuvrageDetailDto {
    private Long id;
    private String designation;
    private String typeHabit;
    private Long couturierId;
    private String couturier;
    private String urlPhotoReference;
    private String consignes;
    private BigDecimal prixConvenu;
    private EtatOuvrageConfection etat;
    private LocalDateTime dateDebutConfection;
    private LocalDateTime datePret;
    private LocalDateTime dateLivraison;
    private String motifAnnulation;
    private List<MesureOuvrageDto> mesures;
    private List<DepotMatiereDetailDto> depotsMatiereClient;
}
