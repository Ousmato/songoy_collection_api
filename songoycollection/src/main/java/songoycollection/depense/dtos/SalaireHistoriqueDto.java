package songoycollection.depense.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Donnees affichees dans l'historique des salaires verses. */
@Data
public class SalaireHistoriqueDto {

    private Long id;

    /** Date et heure auxquelles le versement a ete enregistre. */
    private LocalDateTime datePaiement;

    /** Periode remuneree au format AAAA-MM, par exemple 2026-09. */
    private String periodeSalaire;

    private EnumShared.Entite entite;
    private BigDecimal montant;
    private EnumShared.ModePaiement modePaiement;

    private Long personnelBeneficiaireId;
    private String personnelBeneficiaire;
    private EnumShared.PersonnelRole roleBeneficiaire;

    private Long personnelResponsableId;
    private String personnelResponsable;

    private String libelle;
}
