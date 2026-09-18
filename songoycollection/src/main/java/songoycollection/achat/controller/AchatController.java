package songoycollection.achat.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import songoycollection.achat.dtos.AchatHistoriqueDetailDto;
import songoycollection.achat.dtos.AchatHistoriqueDto;
import java.util.List;
import java.util.UUID;

import songoycollection.achat.dtos.ReceptionRequest;
import songoycollection.achat.dtos.ReceptionResponse;
import songoycollection.shared.ServiceShared;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class AchatController {

    private final ServiceShared serviceShared;

    @PostMapping("receptions/{idAdmin}")
    @Operation(summary = "Recevoir une reception marchandise")
    public ReceptionResponse create(
            @PathVariable Long idAdmin,
            @RequestHeader("Idempotency-Key") UUID receptionKey,
            @Valid @RequestBody ReceptionRequest request
    ) {
        return serviceShared.getReceptionService().create(idAdmin, receptionKey, request);
    }

    @GetMapping("achats/historique/{idAdmin}")
    public List<AchatHistoriqueDto> getHistorique(@PathVariable Long idAdmin) {
        return serviceShared.getAchatService().getHistorique(idAdmin);
    }

    @GetMapping("achats/historique/{achatId}/{idAdmin}")
    public AchatHistoriqueDetailDto getHistoriqueDetail(
            @PathVariable Long achatId,
            @PathVariable Long idAdmin
    ) {
        return serviceShared.getAchatService().getHistoriqueDetail(achatId, idAdmin);
    }
}
