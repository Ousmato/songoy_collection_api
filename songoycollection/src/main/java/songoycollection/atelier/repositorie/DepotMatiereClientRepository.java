package songoycollection.atelier.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.atelier.model.DepotMatiereClient;

import java.util.Collection;
import java.util.List;

@Repository
public interface DepotMatiereClientRepository extends JpaRepository<DepotMatiereClient, Long> {

    @Query("""
            select depot from DepotMatiereClient depot
            join fetch depot.ouvrage
            where depot.ouvrage.id in :ouvrageIds
            order by depot.ouvrage.id asc, depot.id asc
            """)
    List<DepotMatiereClient> findAllByOuvrageIds(@Param("ouvrageIds") Collection<Long> ouvrageIds);
}
