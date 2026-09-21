package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.ArticleVariantAttribute;

import java.util.Collection;
import java.util.List;

@Repository
public interface ArticleVariantAttributeRepository extends JpaRepository<ArticleVariantAttribute, Long> {
    boolean existsByVariantIdAndAttributeId(Long variantId, Long attributeId);

    @Query("""
            select attributeValue
            from ArticleVariantAttribute attributeValue
            join fetch attributeValue.attribute attribute
            where attributeValue.variant.id in :variantIds
            order by attributeValue.variant.id, attributeValue.id
            """)
    List<ArticleVariantAttribute> findAllByVariantIdsWithAttribute(
            @Param("variantIds") Collection<Long> variantIds
    );
}
