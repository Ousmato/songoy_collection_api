package songoycollection.depense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.depense.model.Depense;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {
}
