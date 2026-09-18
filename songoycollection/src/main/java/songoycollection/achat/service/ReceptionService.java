package songoycollection.achat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.dtos.ReceptionRequest;
import songoycollection.achat.dtos.ReceptionResponse;
import songoycollection.reglement.service.ReglementService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceptionService {
    private final ReceptionHelperService helper;
    private final ReglementService reglementService;

    @Transactional
    public ReceptionResponse create(Long idAdmin, UUID receptionKey, ReceptionRequest request) {
        var personnel = helper.validatePersonnel(idAdmin);
        helper.validateRequest(request);
        String reference = helper.reserveReference(receptionKey);
        var fournisseur = helper.resolveFournisseur(request);
        var variants = helper.loadVariants(request);
        Double total = helper.calculateTotal(request);
        Double paid = helper.validatePayment(request.paiement(), total);
        var achat = helper.createAchat(request, reference, personnel, fournisseur);
        var lignes = helper.createLines(achat, request, variants);
        helper.createStock(achat, lignes, personnel);
        if (paid > 0) {
            reglementService.enregistrerPourAchat(
                    achat,
                    BigDecimal.valueOf(paid),
                    request.paiement().modePaiement(),
                    achat.getDate(),
                    personnel
            );
        }
        helper.validateAchat(achat);
        return new ReceptionResponse(
                achat.getId(), reference, total, paid, total - paid,
                "Réception enregistrée avec succès."
        );
    }
}
