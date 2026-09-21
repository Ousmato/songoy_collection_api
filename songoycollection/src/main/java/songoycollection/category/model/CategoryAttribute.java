package songoycollection.category.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "category_attribute", uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "attribute_definition_id"}))
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AttributeDefinition attribute;

    private boolean obligatoire;

    private Integer ordre;

    @NotNull(message = "Le niveau de l'attribut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryAttributeLevel niveau;
}
