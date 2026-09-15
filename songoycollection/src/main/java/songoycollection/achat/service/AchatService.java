package songoycollection.achat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.dtos.AchatHistoriqueDetailDto;
import songoycollection.achat.dtos.AchatHistoriqueDto;
import songoycollection.achat.model.Achat;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AchatService {

    private final AchatHelperService helperService;

    @Transactional(readOnly = true)
    public List<AchatHistoriqueDto> getHistorique(Long idAdmin) {
        helperService.validateAdmin(idAdmin);
        List<Achat> achats = helperService.loadAchats();
        Map<Long, Double> totals = helperService.loadPurchaseTotals();
        Map<Long, Double> paid = helperService.loadPaidTotals();
        Map<Long, Integer> lineCounts = helperService.loadLineCounts();

        return achats.stream()
                .map(achat -> helperService.toSummary(achat, totals, paid, lineCounts))
                .toList();
    }

    @Transactional(readOnly = true)
    public AchatHistoriqueDetailDto getHistoriqueDetail(Long achatId, Long idAdmin) {
        helperService.validateAdmin(idAdmin);
        Achat achat = helperService.loadAchat(achatId);
        AchatHistoriqueDto summary = getSummary(achat);
        return helperService.toDetail(achat, summary);
    }

    private AchatHistoriqueDto getSummary(Achat achat) {
        Map<Long, Double> totals = Map.of(achat.getId(), helperService.loadPurchaseTotal(achat.getId()));
        Map<Long, Double> paid = Map.of(achat.getId(), helperService.loadPaidTotal(achat.getId()));
        Map<Long, Integer> lineCounts = Map.of(achat.getId(), helperService.loadLineCount(achat.getId()));
        return helperService.toSummary(achat, totals, paid, lineCounts);
    }


}
