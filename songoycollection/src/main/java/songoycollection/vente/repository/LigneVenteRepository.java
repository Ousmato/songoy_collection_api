package songoycollection.vente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.vente.model.LigneVente;
import songoycollection.shared.EnumShared;

import java.util.List;

@Repository
public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    @Query("""
            select coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            where l.vente.id = :venteId
              and l.annulee = false
            """)
    Double sumTotalByVenteId(@Param("venteId") Long venteId);

    @Query("""
            select coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            where l.annulee = false
              and l.variant.article.category.entite = :entite
              and l.vente.date >= :debut
              and l.vente.date < :finExclusive
            """)
    Double sumRevenueBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive
    );

    @Query("""
            select l.vente.id, coalesce(sum(l.quantite), 0),
                   coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            where l.annulee = false
              and l.variant.article.category.entite = :entite
              and l.vente.id in :venteIds
            group by l.vente.id
            """)
    List<Object[]> summarizeDashboardSales(
            @Param("entite") EnumShared.Entite entite,
            @Param("venteIds") java.util.Collection<Long> venteIds
    );

    @Query("""
            select a.id,
                   c.nom,
                   articleType.nom,
                   c.mesureCategory,
                   coalesce(sum(l.quantite), 0),
                   coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            join l.vente v
            join l.variant variant
            join variant.article a
            join a.category c
            left join a.articleType articleType
            where l.annulee = false
              and c.entite = :entite
              and v.date >= :debut
              and v.date < :finExclusive
            group by a.id, articleType.nom, c.nom, c.mesureCategory
            order by sum(l.quantite * l.prixVente) desc,
                     sum(l.quantite) desc,
                     a.id asc
            """)
    List<Object[]> aggregateDashboardTopProducts(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive
    );

    @Query("""
            select l.vente.id, v.reference, l.quantite, c.mesureCategory
            from LigneVente l
            join l.variant v
            join v.article a
            join a.category c
            where l.annulee = false
              and c.entite = :entite
              and l.vente.id in :venteIds
            order by l.vente.id, l.id
            """)
    List<Object[]> findDashboardSaleLines(
            @Param("entite") EnumShared.Entite entite,
            @Param("venteIds") java.util.Collection<Long> venteIds
    );

    @Query("""
            select l.vente.id, coalesce(sum(l.quantite * l.prixVente), 0)
            from LigneVente l
            where l.annulee = false
            group by l.vente.id
            """)
    List<Object[]> sumTotalsByVente();

    @Query("""
            select distinct l from LigneVente l
            join fetch l.variant v
            join fetch v.article a
            join fetch a.category c
            left join fetch a.articleType
            left join fetch v.attributs va
            left join fetch va.attribute
            where l.vente.id = :venteId
              and l.annulee = false
            order by l.id
            """)
    List<LigneVente> findActiveLinesForHistoryByVenteId(@Param("venteId") Long venteId);

}
