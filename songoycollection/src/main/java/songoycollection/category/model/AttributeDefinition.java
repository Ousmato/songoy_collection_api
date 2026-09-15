package songoycollection.category.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "attribute_definition", uniqueConstraints = @UniqueConstraint(columnNames = "attribute_key"))
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "attribute_key", nullable = false, unique = true)
    private String key;

    @NotBlank
    private String label;

    @NotBlank
    private String type;
}
