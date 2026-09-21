package songoycollection.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import songoycollection.category.model.CategoryAttribute;
import songoycollection.category.model.CategoryAttributeLevel;

import java.util.List;
@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    boolean existsByCategoryIdAndAttributeId(Long categoryId, Long attributeId);

    void deleteByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "attribute")
    List<CategoryAttribute> findByCategoryIdOrderByOrdreAscIdAsc(Long categoryId);

    @EntityGraph(attributePaths = "attribute")
    List<CategoryAttribute> findByCategoryIdAndNiveauOrderByOrdreAscIdAsc(
            Long categoryId,
            CategoryAttributeLevel niveau
    );
}
