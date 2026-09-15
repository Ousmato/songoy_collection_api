package songoycollection.article.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import songoycollection.category.model.AttributeDefinition;

@Data
@Entity
@Table(name = "article_variant_attribute", uniqueConstraints = @UniqueConstraint(columnNames = {"variant_id", "attribute_id"}))
public class ArticleVariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ArticleVariant variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AttributeDefinition attribute;

    @NotBlank
    private String value;
}
