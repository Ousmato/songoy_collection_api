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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;
import songoycollection.personne.model.Personnel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class OuvrageConfection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La commande de confection est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CommandeConfection commande;

    @ManyToOne(fetch = FetchType.LAZY)
    private Personnel couturier;

    @NotBlank(message = "La designation de l'ouvrage est obligatoire.")
    @Column(nullable = false)
    private String designation;

    private String typeHabit;

    @Column(length = 500)
    private String urlPhotoReference;

    @Column(length = 2_000)
    private String consignes;

    @NotNull(message = "Le prix convenu est obligatoire.")
    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal prixConvenu;

    @NotNull(message = "L'etat de l'ouvrage est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatOuvrageConfection etat = EtatOuvrageConfection.EN_CONFECTION;

    private LocalDateTime dateDebutConfection;

    private LocalDateTime datePret;

    private LocalDateTime dateLivraison;

    @Column(length = 500)
    private String motifAnnulation;
}
