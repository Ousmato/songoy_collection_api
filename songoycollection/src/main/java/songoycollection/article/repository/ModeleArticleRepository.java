package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.ModeleArticle;

import java.util.List;

@Repository
public interface ModeleArticleRepository extends JpaRepository<ModeleArticle, Long> {

    List<ModeleArticle> findByArticleIdOrderByIdAsc(Long articleId);
}
