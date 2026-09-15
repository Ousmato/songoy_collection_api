package songoycollection.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songoycollection.stock.model.StockLot;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, Long> {
    @Query("select lot.variant.id, coalesce(sum(lot.quantityRestante), 0) "
            + "from StockLot lot where lot.variant.article.id = :articleId group by lot.variant.id")
    java.util.List<Object[]> sumQuantitiesByVariantForArticle(@Param("articleId") Long articleId);

    boolean existsByVariantId(Long variantId);

    @Query("select coalesce(sum(lot.quantityRestante), 0) from StockLot lot where lot.variant.article.id = :articleId")
    Double sumQuantityByArticleId(@Param("articleId") Long articleId);
}
