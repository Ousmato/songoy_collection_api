package songoycollection.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import songoycollection.article.model.Declinaison;

import java.util.List;

@Repository
public interface DeclinaisonRepository extends JpaRepository<Declinaison, Long> {

    @EntityGraph(attributePaths = "modeleArticle")
    List<Declinaison> findByModeleArticleIdOrderByIdAsc(Long modeleArticleId);
}
