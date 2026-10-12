package songoycollection.atelier.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.atelier.model.MesureOuvrage;

import java.util.Collection;
import java.util.List;

@Repository
public interface MesureOuvrageRepository extends JpaRepository<MesureOuvrage, Long> {

    @Query("""
            select mesure from MesureOuvrage mesure
            join fetch mesure.ouvrage
            where mesure.ouvrage.id in :ouvrageIds
            order by mesure.ouvrage.id asc, mesure.id asc
            """)
    List<MesureOuvrage> findAllByOuvrageIds(@Param("ouvrageIds") Collection<Long> ouvrageIds);
}
