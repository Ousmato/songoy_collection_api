package songoycollection.atelier.model;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.atelier.model.enums.TypeVersementCommande;
import songoycollection.personne.model.Personnel;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class VersementCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La commande de confection est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_confection_id", nullable = false)
    private CommandeConfection commande;

    @NotNull(message = "Le type de versement est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeVersementCommande type;

    @NotNull(message = "Le montant est obligatoire.")
    @DecimalMin(value = "1", message = "Le montant doit etre positif.")
    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumShared.ModePaiement modePaiement;

    @NotNull(message = "La date du versement est obligatoire.")
    @Column(nullable = false)
    private LocalDateTime dateVersement;

    @NotNull(message = "Le personnel ayant enregistre le versement est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private Personnel enregistrePar;

    @Column(length = 500)
    private String motif;
}
