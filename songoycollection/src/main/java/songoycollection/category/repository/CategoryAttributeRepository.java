package songoycollection.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import songoycollection.category.model.CategoryAttribute;

import java.util.List;
@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    boolean existsByCategoryIdAndAttributeId(Long categoryId, Long attributeId);

    @EntityGraph(attributePaths = "attribute")
    List<CategoryAttribute> findByCategoryIdOrderByOrdreAscIdAsc(Long categoryId);
}
