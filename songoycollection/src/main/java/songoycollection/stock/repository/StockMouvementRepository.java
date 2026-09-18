package songoycollection.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.shared.EnumShared;
import songoycollection.stock.model.StockMouvement;

import java.time.LocalDateTime;

@Repository
public interface StockMouvementRepository extends JpaRepository<StockMouvement, Long> {

    /** Coût d'achat des lots consommés par les ventes sur une période. */
    @Query("""
            select coalesce(sum(m.quantite * ligne.prixAchat), 0)
            from StockMouvement m
            join m.stockLot lot
            join lot.ligneAchat ligne
            where m.type = :type
              and lot.variant.article.category.entite = :entite
              and m.dateEntree >= :debut
              and m.dateEntree < :finExclusive
              and ligne.annulee = false
            """)
    Double sumCostByTypeAndDateBetween(
            @Param("type") EnumShared.TypeMouvement type,
            @Param("entite") EnumShared.Entite entite,
            @Param("debut") LocalDateTime debut,
            @Param("finExclusive") LocalDateTime finExclusive
    );
}
