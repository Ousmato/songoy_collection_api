package songoycollection.depense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.depense.model.Depense;
import songoycollection.shared.EnumShared;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {

    List<Depense> findByDateGreaterThanEqualAndDateLessThanOrderByDateDescIdDesc(
            LocalDateTime debut,
            LocalDateTime finExclusive
    );

    @Query("""
            select coalesce(sum(d.montant), 0)
            from Depense d
            where d.entite = :entite
              and d.date >= :debut
              and d.date < :finExclusive
            """)
    BigDecimal sumAmountBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive
    );

    default BigDecimal sumBoutiqueAmountBetween(LocalDateTime debut, LocalDateTime finExclusive) {
        return sumAmountBetween(EnumShared.Entite.BOUTIQUE, debut, finExclusive);
    }
}
