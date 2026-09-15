package songoycollection.stock;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import songoycollection.article.model.Article;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.category.model.Category;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.stock.dtos.InitialStockRequest;
import songoycollection.stock.model.StockMouvement;
import songoycollection.stock.repository.StockLotRepository;
import songoycollection.stock.service.InitialStockService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InitialStockServiceTest {
    private final ArticleVariantRepository variants = mock(ArticleVariantRepository.class);
    private final StockLotRepository lots = mock(StockLotRepository.class);
    private final PersonnelUtiliShared personnel = mock(PersonnelUtiliShared.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final InitialStockService service = new InitialStockService(
            variants, lots, personnel, entityManager
    );
    private Category category;

    @BeforeEach
    void setup() {
        category = new Category();
        category.setMesureCategory(EnumShared.UniteMesureCategory.UNITE);
        Article article = new Article();
        article.setCategory(category);
        ArticleVariant variant = new ArticleVariant();
        variant.setId(1L);
        variant.setArticle(article);
        when(variants.findForStockUpdate(1L)).thenReturn(Optional.of(variant));
        when(variants.findById(1L)).thenReturn(Optional.of(variant));
        when(lots.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void existingLotBlocksInitialStockRegardlessOfRemainingQuantity() {
        when(lots.existsByVariantId(1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, 3L, request("10")));
        verify(lots, never()).save(any());
    }

    @Test
    void availabilityIsFalseWhenALotAlreadyExists() {
        when(lots.existsByVariantId(1L)).thenReturn(true);
        assertFalse(service.isAvailable(1L, 3L));
    }

    @Test
    void rejectsFractionalUnits() {
        assertThrows(IllegalArgumentException.class, () -> service.create(1L, 3L, request("1.5")));
        verify(lots, never()).save(any());
    }

    @Test
    void acceptsMetresAndCreatesLotHistoryAndMovement() {
        category.setMesureCategory(EnumShared.UniteMesureCategory.METRE);
        service.create(1L, 3L, request("12.5"));
        verify(lots).save(argThat(lot -> lot.getInitialQuantity() == 12.5
                && lot.getQuantityRestante() == 12.5
                && lot.getEntryDate().equals(LocalDate.now())
                && "Inventaire".equals(lot.getObservation())
                && lot.getCreatedAt() != null));
        verify(entityManager).persist(any(StockMouvement.class));
    }

    private InitialStockRequest request(String quantity) {
        return new InitialStockRequest(new BigDecimal(quantity), LocalDate.now(), "Inventaire");
    }
}
