package songoycollection.atelier.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import songoycollection.atelier.model.OuvrageConfection;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OuvrageConfectionRepository extends JpaRepository<OuvrageConfection, Long> {

    @Query("""
            select o from OuvrageConfection o
            join fetch o.commande
            left join fetch o.couturier
            where o.commande.id in :commandeIds
            order by o.commande.id asc, o.id asc
            """)
    List<OuvrageConfection> findAllByCommandeIdsWithCouturier(@Param("commandeIds") Collection<Long> commandeIds);

    long countByEtat(EtatOuvrageConfection etat);

    long countByEtatAndDateLivraisonGreaterThanEqualAndDateLivraisonLessThan(
            EtatOuvrageConfection etat,
            LocalDateTime debut,
            LocalDateTime fin
    );

    @Query("""
            select o.commande.id, sum(o.prixConvenu) from OuvrageConfection o
            where o.commande.id in :commandeIds
            group by o.commande.id
            """)
    List<Object[]> sumPrixConvenuByCommandeIds(@Param("commandeIds") Collection<Long> commandeIds);
}
