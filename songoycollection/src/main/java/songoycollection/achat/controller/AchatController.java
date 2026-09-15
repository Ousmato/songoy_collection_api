package songoycollection.achat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import songoycollection.achat.dtos.AchatHistoriqueDetailDto;
import songoycollection.achat.dtos.AchatHistoriqueDto;
import java.util.List;
import songoycollection.shared.ServiceShared;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class AchatController {

    private final ServiceShared serviceShared;

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
