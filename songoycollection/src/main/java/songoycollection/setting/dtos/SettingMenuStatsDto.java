package songoycollection.setting.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Indicateurs du menu de paramétrage pour l'entité et le mois courants. */
@Data
public class SettingMenuStatsDto {
    private EnumShared.Entite entite;
    private LocalDate debutMois;
    private LocalDate finMois;
    private long nombreCategoriesActives;
    private long nombreTypesArticlesUtilises;
    private BigDecimal montantSalairesMois;
    private BigDecimal montantAutresDepensesMois;
}
