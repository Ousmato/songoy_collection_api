package songoycollection.dashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import songoycollection.shared.EnumShared;

import java.util.EnumMap;
import java.util.Map;

/** Seuils d'alerte stock définis dans le fichier .env. */
@Component
public class StockAlertThresholdProperties {

    private final Map<EnumShared.UniteMesureCategory, Thresholds> thresholds;

    public StockAlertThresholdProperties(
            @Value("${STOCK_SEUIL_ALERTE_UNITE}") Double alerteUnite,
            @Value("${STOCK_SEUIL_CRITIQUE_UNITE}") Double critiqueUnite,
            @Value("${STOCK_SEUIL_ALERTE_METRE}") Double alerteMetre,
            @Value("${STOCK_SEUIL_CRITIQUE_METRE}") Double critiqueMetre,
            @Value("${STOCK_SEUIL_ALERTE_PAGNE}") Double alertePagne,
            @Value("${STOCK_SEUIL_CRITIQUE_PAGNE}") Double critiquePagne,
            @Value("${STOCK_SEUIL_ALERTE_CARTON}") Double alerteCarton,
            @Value("${STOCK_SEUIL_CRITIQUE_CARTON}") Double critiqueCarton,
            @Value("${STOCK_SEUIL_CRITIQUE_PAIRE}") Double critiquePaire,
            @Value("${STOCK_SEUIL_ALERTE_PAIRE}") Double alertePaire


    ) {
        EnumMap<EnumShared.UniteMesureCategory, Thresholds> values =
                new EnumMap<>(EnumShared.UniteMesureCategory.class);
        values.put(EnumShared.UniteMesureCategory.UNITE, new Thresholds(alerteUnite, critiqueUnite));
        values.put(EnumShared.UniteMesureCategory.METRE, new Thresholds(alerteMetre, critiqueMetre));
        values.put(EnumShared.UniteMesureCategory.PAGNE, new Thresholds(alertePagne, critiquePagne));
        values.put(EnumShared.UniteMesureCategory.CARTON, new Thresholds(alerteCarton, critiqueCarton));
        values.put(EnumShared.UniteMesureCategory.PAIRE, new Thresholds(alertePaire, critiquePaire));
        this.thresholds = Map.copyOf(values);
    }

    public Thresholds forUnit(EnumShared.UniteMesureCategory unite) {
        if (unite == null) {
            throw new IllegalStateException("La catégorie du stock n'a pas d'unité de mesure.");
        }
        Thresholds value = thresholds.get(unite);
        if (value == null) {
            throw new IllegalStateException("Aucun seuil de stock configuré pour l'unité " + unite + ".");
        }
        return value;
    }

    public record Thresholds(Double alerte, Double critique) {
        public Thresholds {
            if (alerte == null || critique == null || critique < 0 || alerte < critique) {
                throw new IllegalArgumentException("Les seuils de stock sont invalides.");
            }
        }
    }
}
