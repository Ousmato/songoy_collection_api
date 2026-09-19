package songoycollection.stock.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import songoycollection.shared.EnumShared;
import songoycollection.stock.dtos.StockMouvementHistoriqueDto;
import songoycollection.stock.service.StockMouvementService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/stock")
@RequiredArgsConstructor
public class StockMouvementController {

    private final StockMouvementService stockMouvementService;

    @GetMapping("/mouvements/{idAdmin}")
    @Operation(summary = "Récupérer les mouvements de stock de l'entité")
    public List<StockMouvementHistoriqueDto> getHistorique(
            @PathVariable Long idAdmin,
            @RequestParam(required = false) EnumShared.Entite entite,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) EnumShared.TypeMouvement type,
            @RequestParam(required = false) String recherche
    ) {
        return stockMouvementService.getHistorique(idAdmin, entite, debut, fin, type, recherche);
    }
}
