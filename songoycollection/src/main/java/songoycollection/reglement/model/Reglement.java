package songoycollection.reglement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.achat.model.Achat;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;
import songoycollection.vente.model.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Paiement réellement effectué pour un achat ou une vente.
 *
 * <p>Un règlement doit être rattaché à un seul document : achat ou vente.
 * Cette règle sera contrôlée dans le service de règlement.</p>
 */
@Entity
@Table(name = "reglement")
@Data
public class Reglement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achat_id")
    private Achat achat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id")
    private Vente vente;

    @NotNull(message = "Le montant du règlement est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant du règlement doit être positif.")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumShared.ModePaiement modePaiement;

    @NotNull(message = "La date du règlement est obligatoire.")
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull(message = "Le personnel responsable est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @Column(unique = true, length = 100)
    private String reference;
}
