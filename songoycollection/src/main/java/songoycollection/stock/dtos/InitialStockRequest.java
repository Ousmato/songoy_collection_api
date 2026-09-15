package songoycollection.stock.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record InitialStockRequest(
        @NotNull @DecimalMin("0.001") @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @NotNull @PastOrPresent LocalDate date,
        @Size(max = 500) String observation
) {}
