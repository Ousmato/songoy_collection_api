package songoycollection.article.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import songoycollection.category.model.AttributeDefinition;

@Data
@Entity
@Table(
        name = "declinaison_attribute",
        uniqueConstraints = @UniqueConstraint(columnNames = {"declinaison_id", "attribute_id"})
)
public class DeclinaisonAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "declinaison_id", nullable = false)
    private Declinaison declinaison;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeDefinition attribute;

    @NotBlank
    private String value;
}
