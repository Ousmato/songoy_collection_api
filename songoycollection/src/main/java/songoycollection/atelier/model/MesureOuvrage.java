package songoycollection.atelier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class MesureOuvrage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ouvrage est obligatoire.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OuvrageConfection ouvrage;

    @NotBlank(message = "Le libelle de la mesure est obligatoire.")
    @Column(nullable = false)
    private String libelle;

    @NotNull(message = "La valeur de la mesure est obligatoire.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valeur;

    @Column(nullable = false, length = 20)
    private String unite = "cm";
}
