package songoycollection.reglement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.reglement.model.Reglement;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReglementRepository extends JpaRepository<Reglement, Long> {

    List<Reglement> findByAchatIdOrderByDateAscIdAsc(Long achatId);

    List<Reglement> findByVenteIdOrderByDateAscIdAsc(Long venteId);

    @Query("""
            select coalesce(sum(r.montant), 0)
            from Reglement r
            where r.achat.id = :achatId
            """)
    BigDecimal sumByAchatId(@Param("achatId") Long achatId);

    @Query("""
            select coalesce(sum(r.montant), 0)
            from Reglement r
            where r.vente.id = :venteId
            """)
    BigDecimal sumByVenteId(@Param("venteId") Long venteId);

    @Query("""
            select r.achat.id, coalesce(sum(r.montant), 0)
            from Reglement r
            where r.achat is not null
            group by r.achat.id
            """)
    List<Object[]> sumByAchat();

    @Query("""
            select r.vente.id, coalesce(sum(r.montant), 0)
            from Reglement r
            where r.vente is not null
            group by r.vente.id
            """)
    List<Object[]> sumByVente();
}
