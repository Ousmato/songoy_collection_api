package songoycollection.depense.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;

/** Indicateurs de paiement de salaire pour une période rémunérée. */
@Data
public class SalaireStatistiquesDto {

    /** Période rémunérée au format AAAA-MM. */
    private String periodeSalaire;

    /** Entité retenue, ou null lorsque toutes les entités sont regroupées. */
    private EnumShared.Entite entite;

    /** Somme de tous les versements de salaire enregistrés pour cette période. */
    private BigDecimal montantVerse;

    /** Personnel actif ayant reçu au moins un versement strictement positif. */
    private long nombrePersonnesPayees;

    /** Personnel actif concerné dont le total de versements est égal à zéro. */
    private long nombrePersonnesNonPayees;
}
