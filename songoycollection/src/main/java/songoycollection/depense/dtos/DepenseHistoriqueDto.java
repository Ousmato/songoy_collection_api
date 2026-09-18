package songoycollection.depense.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Données affichées dans l'historique des dépenses. */
@Data
public class DepenseHistoriqueDto {
    private Long id;
    private LocalDateTime date;
    private EnumShared.MotifDepense motif;
    private EnumShared.Entite entite;
    private BigDecimal montant;
    private EnumShared.ModePaiement modePaiement;
    private Long personnelResponsableId;
    private String personnelResponsable;
    private Long personnelBeneficiaireId;
    private String personnelBeneficiaire;
    private Long fournisseurId;
    private String fournisseur;
    private String libelle;
}
