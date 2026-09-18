package songoycollection.dashboard.dtos;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Indicateurs principaux affichés sur le tableau de bord.
 *
 * <p>Les montants sont retournés comme des nombres en FCFA. Le formatage,
 * les séparateurs de milliers et le libellé de la devise sont gérés par le
 * frontend.</p>
 */
@Data
public class DashboardIndicateursDto {

    /** CA des lignes de vente non annulées sur la période sélectionnée. */
    private BigDecimal caJour = BigDecimal.ZERO;

    /** CA des lignes de vente non annulées sur le mois de référence. */

    /** Nombre de ventes distinctes ayant au moins une ligne active sur la période. */
    private long ventesJour;

    /**
     * Résultat estimé de la période : CA - coût des articles vendus - dépenses
     * imputées à l'entité sélectionnée.
     */
    private BigDecimal beneficeEstime = BigDecimal.ZERO;

    /**
     * Valeur comptable du stock disponible au coût d'achat :
     * quantité restante du lot × prix d'achat de sa ligne de réception.
     */
    private BigDecimal valeurStock = BigDecimal.ZERO;
}
