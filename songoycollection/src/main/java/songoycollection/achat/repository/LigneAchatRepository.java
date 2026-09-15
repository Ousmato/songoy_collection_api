package songoycollection.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.achat.model.LigneAchat;

import java.util.List;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    @Query("""
            select l.achat.id, coalesce(sum(l.quantite * l.prixAchat), 0)
            from LigneAchat l
            where l.annulee = false
            group by l.achat.id
            """)
    List<Object[]> sumTotalsByAchat();

    @Query("""
            select l.achat.id, count(l.id)
            from LigneAchat l
            where l.annulee = false
            group by l.achat.id
            """)
    List<Object[]> countActiveLinesByAchat();

    @Query("""
            select coalesce(sum(l.quantite * l.prixAchat), 0)
            from LigneAchat l
            where l.achat.id = :achatId
              and l.annulee = false
            """)
    Double sumTotalByAchatId(@Param("achatId") Long achatId);

    @Query("""
            select count(l.id)
            from LigneAchat l
            where l.achat.id = :achatId
              and l.annulee = false
            """)
    Long countActiveLinesByAchatId(@Param("achatId") Long achatId);

    @Query("""
            select l from LigneAchat l
            join fetch l.variant v
            join fetch v.article a
            join fetch a.category c
            left join fetch a.articleType t
            where l.achat.id = :achatId
              and l.annulee = false
            order by l.id
            """)
    List<LigneAchat> findActiveLinesForHistory(@Param("achatId") Long achatId);
}
