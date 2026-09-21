package songoycollection.vente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.vente.model.Vente;
import songoycollection.shared.EnumShared;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    @Query("""
            select v from Vente v
            left join fetch v.client
            left join fetch v.caissier
            order by v.date desc, v.id desc
            """)
    List<Vente> findAllForHistory();

    @Query("""
            select v from Vente v
            left join fetch v.client
            left join fetch v.caissier
            where v.id = :venteId
            """)
    Optional<Vente> findForHistoryById(@Param("venteId") Long venteId);

    @Query("""
            select count(distinct v.id)
            from Vente v
            join LigneVente l on l.vente = v
            where l.annulee = false
              and l.variant.declinaison.modeleArticle.article.category.entite = :entite
              and v.date >= :debut
              and v.date < :finExclusive
            """)
    long countActiveBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive
    );

    @Query("""
            select distinct v
            from Vente v
            join LigneVente l on l.vente = v
            left join fetch v.client
            where l.annulee = false
              and l.variant.declinaison.modeleArticle.article.category.entite = :entite
              and v.date >= :debut
              and v.date < :finExclusive
            order by v.date desc, v.id desc
            """)
    List<Vente> findDashboardSalesBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive
    );
}
