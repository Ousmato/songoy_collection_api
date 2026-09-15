package songoycollection.achat.dtos;

import lombok.Data;
import songoycollection.shared.EnumShared;

import java.time.LocalDateTime;

/** Données synthétiques utilisées par la liste de l'historique des achats. */
@Data
public class AchatHistoriqueDto {
    private Long id;
    private String reference;
    private LocalDateTime date;
    private LocalDateTime dateValidation;
    private EnumShared.StatutAchat statut;
    private EnumShared.Entite entite;

    /** Nom du fournisseur enregistré ou nomFournisseur lorsqu'il est libre. */
    private String fournisseur;
    private String numeroFacture;

    private Double totalAchat;
    private Double montantPaye;
    private Double resteAPayer;
    private EnumShared.StatutPaiement statutPaiement;
    private Integer nombreLignes;
}
