package songoycollection.atelier.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import songoycollection.atelier.model.CommandeConfection;
import songoycollection.atelier.model.enums.EtatOuvrageConfection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeConfectionRepository extends JpaRepository<CommandeConfection, Long> {

    @Query("""
            select c from CommandeConfection c
            left join fetch c.client
            where exists (
                select 1 from OuvrageConfection o
                where o.commande.id = c.id and o.etat in :etats
            )
            order by c.dateLivraisonPrevue asc, c.id asc
            """)
    List<CommandeConfection> findAllWithOuvrageInStates(
            @Param("etats") Collection<EtatOuvrageConfection> etats
    );

    @Query("""
            select c.id from CommandeConfection c
            where exists (
                select 1 from OuvrageConfection o
                where o.commande.id = c.id and o.etat in :etats
            )
            """)
    List<Long> findIdsWithOuvrageInStates(@Param("etats") Collection<EtatOuvrageConfection> etats);

    @Query("""
            select c from CommandeConfection c
            left join fetch c.client
            left join fetch c.creePar
            where c.id = :commandeId
            """)
    Optional<CommandeConfection> findByIdWithClientAndCreator(@Param("commandeId") Long commandeId);

}
