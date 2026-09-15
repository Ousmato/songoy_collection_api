package songoycollection.achat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.achat.model.Achat;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchatRepository extends JpaRepository<Achat, Long> {

    @Query("""
            select a from Achat a
            left join fetch a.fournisseur
            left join fetch a.adminResponsable
            order by a.date desc, a.id desc
            """)
    List<Achat> findAllForHistory();

    @Query("""
            select a from Achat a
            left join fetch a.fournisseur
            left join fetch a.adminResponsable
            where a.id = :achatId
            """)
    Optional<Achat> findForHistoryById(@Param("achatId") Long achatId);
}
