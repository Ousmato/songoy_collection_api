package songoycollection.vente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songoycollection.vente.model.Vente;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {
}
