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
            select d
            from Depense d
            join fetch d.personnelResponsable
            left join fetch d.personnelBeneficiaire
            where d.motifDepense = :motif
              and (:entite is null or d.entite = :entite)
              and (:periodeSalaire is null or d.periodeSalaire = :periodeSalaire)
            order by d.date desc, d.id desc
            """)
    List<Depense> findHistoriqueSalaires(
            @Param("motif") EnumShared.MotifDepense motif,
            @Param("entite") EnumShared.Entite entite,
            @Param("periodeSalaire") String periodeSalaire
    );

    @Query("""
            select coalesce(sum(d.montant), 0)
            from Depense d
            where d.motifDepense = :motif
              and d.periodeSalaire = :periodeSalaire
              and (:entite is null or d.entite = :entite)
            """)
    BigDecimal sumMontantSalairesParPeriode(
            @Param("motif") EnumShared.MotifDepense motif,
            @Param("periodeSalaire") String periodeSalaire,
            @Param("entite") EnumShared.Entite entite
    );

    @Query("""
            select count(distinct beneficiaire.id)
            from Depense d
            join d.personnelBeneficiaire beneficiaire
            where d.motifDepense = :motif
              and d.periodeSalaire = :periodeSalaire
              and d.montant > 0
              and beneficiaire.active = true
              and beneficiaire.role not in :rolesExclus
              and (:entite is null or d.entite = :entite)
              and (:entite is null or beneficiaire.entite = :entite)
            """)
    long countBeneficiairesPayesParPeriode(
            @Param("motif") EnumShared.MotifDepense motif,
            @Param("periodeSalaire") String periodeSalaire,
            @Param("entite") EnumShared.Entite entite,
            @Param("rolesExclus") List<EnumShared.PersonnelRole> rolesExclus
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

    @Query("""
            select coalesce(sum(d.montant), 0)
            from Depense d
            where d.entite = :entite
              and d.motifDepense = :motif
              and d.date >= :debut
              and d.date < :finExclusive
            """)
    BigDecimal sumAmountByEntiteAndMotifBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("motif") EnumShared.MotifDepense motif,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive
    );

    @Query("""
            select coalesce(sum(d.montant), 0)
            from Depense d
            where d.entite = :entite
              and (d.motifDepense is null or d.motifDepense <> :motifExclu)
              and d.date >= :debut
              and d.date < :finExclusive
            """)
    BigDecimal sumAmountByEntiteAndMotifDifferentBetween(
            @Param("entite") EnumShared.Entite entite,
            @Param("motifExclu") EnumShared.MotifDepense motifExclu,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive
    );

    default BigDecimal sumBoutiqueAmountBetween(LocalDateTime debut, LocalDateTime finExclusive) {
        return sumAmountBetween(EnumShared.Entite.BOUTIQUE, debut, finExclusive);
    }
}
