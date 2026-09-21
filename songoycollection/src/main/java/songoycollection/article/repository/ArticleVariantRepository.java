package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.ArticleVariant;

import java.util.List;
import java.util.Collection;

@Repository
public interface ArticleVariantRepository extends JpaRepository<ArticleVariant, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select variant from ArticleVariant variant where variant.id = :id")
    java.util.Optional<ArticleVariant> findForStockUpdate(@Param("id") Long id);

    boolean existsByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCaseAndIdNot(String reference, Long id);

    @EntityGraph(attributePaths = {"attributs", "attributs.attribute"})
    List<ArticleVariant> findByDeclinaisonIdOrderByIdAsc(Long declinaisonId);

    @Query("select distinct variant.declinaison.id from ArticleVariant variant "
            + "where variant.declinaison.id in :declinaisonIds")
    List<Long> findDeclinaisonIdsWithVariants(@Param("declinaisonIds") Collection<Long> declinaisonIds);

    boolean existsByDeclinaisonId(Long declinaisonId);

    @EntityGraph(attributePaths = {
            "declinaison",
            "declinaison.modeleArticle",
            "declinaison.modeleArticle.article",
            "declinaison.modeleArticle.article.category",
            "attributs",
            "attributs.attribute"
    })
    java.util.Optional<ArticleVariant> findById(Long id);

    @Query("select min(variant.prixVente) from ArticleVariant variant "
            + "where variant.declinaison.modeleArticle.article.id = :articleId")
    Integer findMinimumPriceByArticleId(@Param("articleId") Long articleId);
}
