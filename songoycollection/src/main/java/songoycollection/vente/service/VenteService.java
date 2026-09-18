package songoycollection.vente.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.config.HttpCustumMessage;
import songoycollection.reglement.service.ReglementService;
import songoycollection.personne.model.Personnel;
import songoycollection.vente.dtos.VenteHistoriqueDetailDto;
import songoycollection.vente.dtos.VenteHistoriqueDto;
import songoycollection.vente.dtos.VenteRequestDto;
import songoycollection.vente.model.LigneVente;
import songoycollection.vente.model.Vente;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VenteService {

    private final VenteHelperService helper;
    private final ReglementService reglementService;

    @Transactional
    public HttpCustumMessage addVente(VenteRequestDto dto, Long idAdmin) {
        Personnel caissier = helper.validatePersonnel(idAdmin);
        helper.validateRequest(dto);

        Personnel client = helper.resolveClient(dto);
        Map<Long, songoycollection.article.model.ArticleVariant> variants = helper.loadVariants(dto);
        double total = helper.calculateTotal(dto);
        double paid = helper.resolvePaidAmount(dto, total);
        Vente vente = helper.createVente(dto, caissier, client);
        List<LigneVente> lines = helper.createLines(vente, dto, variants);
        helper.removeFromStock(vente, lines);

        if (paid > 0) {
            reglementService.enregistrerPourVente(
                    vente,
                    BigDecimal.valueOf(paid),
                    dto.getModePaiement(),
                    vente.getDate(),
                    caissier
            );
        }
        return HttpCustumMessage.addMessage();
    }

    @Transactional(readOnly = true)
    public List<VenteHistoriqueDto> getHistoriqueVente(Long idAdmin) {
        helper.validateAdmin(idAdmin);

        List<Vente> ventes = helper.loadVentesForHistory();
        if (ventes.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> totals = helper.loadSaleTotals();
        Map<Long, Double> paid = helper.loadPaidTotals();

        return ventes.stream()
                .map(vente -> VenteHistoriqueDto.fromVente(
                        vente,
                        BigDecimal.valueOf(totals.getOrDefault(vente.getId(), 0.0)),
                        BigDecimal.valueOf(paid.getOrDefault(vente.getId(), 0.0))
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public VenteHistoriqueDetailDto getHistoriqueDetailVente(Long idAdmin, Long idVente) {
        helper.validateAdmin(idAdmin);

        Vente vente = helper.loadVenteForHistoryDetail(idVente);
        VenteHistoriqueDto summary = VenteHistoriqueDto.fromVente(
                vente,
                BigDecimal.valueOf(helper.loadSaleTotal(idVente)),
                BigDecimal.valueOf(helper.loadPaidTotal(idVente))
        );

        VenteHistoriqueDetailDto detail = VenteHistoriqueDetailDto.fromSummary(summary);
        detail.setLignes(helper.loadHistoryLines(idVente));
        detail.setPaiements(helper.loadHistoryPayments(idVente));
        return detail;
    }



}
