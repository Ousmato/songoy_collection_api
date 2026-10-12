package songoycollection.atelier.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.atelier.model.VersementCommande;

import java.util.Collection;
import java.util.List;

@Repository
public interface VersementCommandeRepository extends JpaRepository<VersementCommande, Long> {
    @Query("""
            select v from VersementCommande v
            join fetch v.commande
            where v.commande.id in :commandeIds
            """)
    List<VersementCommande> findAllByCommandeIds(@Param("commandeIds") Collection<Long> commandeIds);
}
