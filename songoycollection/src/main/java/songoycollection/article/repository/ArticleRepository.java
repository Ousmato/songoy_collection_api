package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.Article;
import songoycollection.shared.EnumShared;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("""
        SELECT a.category.id, COUNT(a)
        FROM Article a
        WHERE a.category.id IN :ids
        GROUP BY a.category.id
    """)
    List<Object[]> countArticlesByCategoryIds(@Param("ids") List<Long> ids);

    long countByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "articleType"})
    Optional<Article> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {"category", "articleType"})
    List<Article> findByActiveTrueOrderByIdDesc();

    @EntityGraph(attributePaths = {"category", "articleType"})
    List<Article> findByCategoryIdAndActiveTrueOrderByIdDesc(Long categoryId);

    boolean existsByCategoryIdAndArticleTypeId(Long categoryId, Long articleId);

    @Query("""
        select count(distinct article.articleType.id)
        from Article article
        where article.active = true
          and article.articleType is not null
          and article.category.entite = :entite
    """)
    long countDistinctActiveTypesByEntite(@Param("entite") EnumShared.Entite entite);

}
