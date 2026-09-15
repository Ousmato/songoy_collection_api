package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.ArticleVariantAttribute;

@Repository
public interface ArticleVariantAttributeRepository extends JpaRepository<ArticleVariantAttribute, Long> {
    boolean existsByVariantIdAndAttributeId(Long variantId, Long attributeId);
}
