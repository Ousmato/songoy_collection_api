package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.ArticleType;
@Repository
public interface ArticleTypeRepository extends JpaRepository<ArticleType, Long> {
}
