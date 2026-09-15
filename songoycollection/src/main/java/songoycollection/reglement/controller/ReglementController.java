package songoycollection.reglement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import songoycollection.reglement.dtos.ReglementRequest;
import songoycollection.reglement.dtos.ReglementResponse;
import songoycollection.reglement.service.ReglementService;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class ReglementController {

    private final ReglementService reglementService;

    @PostMapping("/achats/{achatId}/reglements/{idAdmin}")
    public ReglementResponse reglerAchat(
            @PathVariable Long achatId,
            @PathVariable Long idAdmin,
            @Valid @RequestBody ReglementRequest request
    ) {
        return reglementService.reglerAchat(achatId, idAdmin, request);
    }

    @PostMapping("/ventes/{venteId}/reglements/{idAdmin}")
    public ReglementResponse reglerVente(
            @PathVariable Long venteId,
            @PathVariable Long idAdmin,
            @Valid @RequestBody ReglementRequest request
    ) {
        return reglementService.reglerVente(venteId, idAdmin, request);
    }
}
