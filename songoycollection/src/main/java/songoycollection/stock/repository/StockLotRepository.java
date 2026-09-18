package songoycollection.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.stock.model.StockLot;
import songoycollection.shared.EnumShared;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lot from StockLot lot "
            + "where lot.variant.id = :variantId "
            + "and lot.quantityRestante > 0 "
            + "order by lot.entryDate asc, lot.createdAt asc, lot.id asc")
    List<StockLot> findAvailableByVariantIdForUpdate(@Param("variantId") Long variantId);

    @Query("select lot.variant.id, coalesce(sum(lot.quantityRestante), 0) "
            + "from StockLot lot where lot.variant.article.id = :articleId group by lot.variant.id")
    java.util.List<Object[]> sumQuantitiesByVariantForArticle(@Param("articleId") Long articleId);

    boolean existsByVariantId(Long variantId);

    @Query("select coalesce(sum(lot.quantityRestante), 0) from StockLot lot where lot.variant.article.id = :articleId")
    Double sumQuantityByArticleId(@Param("articleId") Long articleId);

    @Query("""
            select v.id,
                   a.id,
                   c.nom,
                   articleType.nom,
                   v.reference,
                   c.mesureCategory,
                   coalesce(sum(lot.quantityRestante), 0)
            from StockLot lot
            join lot.variant v
            join v.article a
            join a.category c
            left join a.articleType articleType
            where c.entite = :entite
            group by v.id, a.id, articleType.nom, c.nom, v.reference, c.mesureCategory
            order by sum(lot.quantityRestante) asc, v.id asc
            """)
    List<Object[]> aggregateDashboardStockByVariant(
            @Param("entite") EnumShared.Entite entite
    );

    @Query("""
            select coalesce(sum(lot.quantityRestante * ligne.prixAchat), 0)
            from StockLot lot
            join lot.ligneAchat ligne
            where lot.quantityRestante > 0
              and lot.variant.article.category.entite = :entite
              and ligne.annulee = false
            """)
    Double sumAccountingValue(@Param("entite") EnumShared.Entite entite);
}
