package songoycollection.stock.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.stock.dtos.InitialStockRequest;
import songoycollection.stock.service.InitialStockService;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class InitialStockController {
    private final InitialStockService service;

    @GetMapping("initial-stock/{variantId}/{idAdmin}")
    public Availability availability(@PathVariable Long variantId, @PathVariable Long idAdmin) {
        return new Availability(service.isAvailable(variantId, idAdmin));
    }

    @PostMapping("initial-stock/{variantId}/{idAdmin}")
    public Message create(
            @PathVariable Long variantId,
            @PathVariable Long idAdmin,
            @Valid @RequestBody InitialStockRequest request
    ) {
        service.create(variantId, idAdmin, request);
        return new Message("Stock initial enregistré avec succès.");
    }

    public record Availability(boolean available) {}
    public record Message(String message) {}
}
