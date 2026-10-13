package songoycollection.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import songoycollection.dashboard.dtos.DashboardIndicateursDto;
import songoycollection.dashboard.dtos.DashboardAlerteStockDto;
import songoycollection.dashboard.dtos.DashboardTopProduitDto;
import songoycollection.dashboard.dtos.DashboardVenteJourDto;
import songoycollection.dashboard.service.DashboardService;
import songoycollection.config.security.PersonnelPrincipal;
import songoycollection.shared.EnumShared;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/indicateurs")
    @Operation(summary = "Récupérer les indicateurs principaux du dashboard")
    public DashboardIndicateursDto getIndicateurs(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestParam EnumShared.Entite entite,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return service.getIndicateurs(connectedPersonnel.getId(), entite, date, debut, fin);
    }

    @GetMapping("/ventes-jour")
    @Operation(summary = "Récupérer les ventes de la journée")
    public List<DashboardVenteJourDto> getVentesDuJour(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestParam EnumShared.Entite entite,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.getVentesDuJour(connectedPersonnel.getId(), entite, date);
    }

    @GetMapping("/alertes-stock")
    @Operation(summary = "Récupérer les alertes de stock")
    public List<DashboardAlerteStockDto> getAlertesStock(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestParam EnumShared.Entite entite
    ) {
        return service.getAlertesStock(connectedPersonnel.getId(), entite);
    }

    @GetMapping("/top-produits")
    @Operation(summary = "Récupérer les produits les plus vendus sur une période")
    public List<DashboardTopProduitDto> getTopProduits(
            @AuthenticationPrincipal PersonnelPrincipal connectedPersonnel,
            @RequestParam EnumShared.Entite entite,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return service.getTopProduits(connectedPersonnel.getId(), entite, date, debut, fin);
    }
}
