package songoycollection.vente.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import songoycollection.config.HttpCustumMessage;
import songoycollection.shared.ServiceShared;
import songoycollection.vente.dtos.ReglementVenteReqestDto;
import songoycollection.vente.dtos.VenteHistoriqueDetailDto;
import songoycollection.vente.dtos.VenteHistoriqueDto;
import songoycollection.vente.dtos.VenteRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class VenteController {

    private final ServiceShared serviceShared;

    @PostMapping("add-vente/{idAdmin}")
    @Operation(summary = "Ajouter une vente")
    public HttpCustumMessage addVente(
            @Valid @RequestBody VenteRequestDto dto,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getVenteService().addVente(dto, idAdmin);
    }

    @GetMapping("get-historique-vente/{idAdmin}")
    @Operation(summary = "Recuperer l'historique  ventes ")
    public List<VenteHistoriqueDto> getHistoriqueVente(@PathVariable Long idAdmin) {
        return serviceShared.getVenteService().getHistoriqueVente(idAdmin);
    }

    @GetMapping("get-historique-detail-vente/{idAdmin}/{idVente}")
    @Operation(summary = "Recuperer l'historique de details vente ")
    public VenteHistoriqueDetailDto getHistoriqueVente(
            @PathVariable Long idAdmin,
            @PathVariable Long idVente
    ) {
        return serviceShared.getVenteService().getHistoriqueDetailVente(idAdmin, idVente);
    }




}
