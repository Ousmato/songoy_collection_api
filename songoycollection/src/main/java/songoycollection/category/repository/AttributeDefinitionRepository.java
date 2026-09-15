package songoycollection.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.category.model.AttributeDefinition;

import java.util.Optional;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    Optional<AttributeDefinition> findByKey(String key);
}
