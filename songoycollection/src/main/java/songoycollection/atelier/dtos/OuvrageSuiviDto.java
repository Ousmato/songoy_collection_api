package songoycollection.atelier.dtos;

import lombok.Data;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;

import java.util.List;

@Data
public class OuvrageSuiviDto {
    private Long id;
    private String designation;
    private String typeHabit;
    private Long couturierId;
    private String couturier;
    private String urlPhotoReference;
    private List<DepotMatiereSuiviDto> depotsMatiereClient;
    private EtatOuvrageConfection etat;
}
