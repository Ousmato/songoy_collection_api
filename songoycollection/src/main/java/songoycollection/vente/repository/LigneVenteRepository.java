package songoycollection.vente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.vente.model.LigneVente;

@Repository
public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    @Query("""
            select coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            where l.vente.id = :venteId
              and l.annulee = false
            """)
    Double sumTotalByVenteId(@Param("venteId") Long venteId);
}
