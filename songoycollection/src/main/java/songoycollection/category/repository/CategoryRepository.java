package songoycollection.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import songoycollection.category.model.Category;
import songoycollection.shared.EnumShared;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActive(boolean active);

    long countByActiveTrueAndEntite(EnumShared.Entite entite);

    @Query("""
            select distinct category
            from Category category
            join CategoryAttribute attribute on attribute.category.id = category.id
            where category.active = true
            order by category.nom
            """)
    List<Category> findActiveWithAttributes();
}
