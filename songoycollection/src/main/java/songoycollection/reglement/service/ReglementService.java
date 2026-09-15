package songoycollection.reglement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.reglement.dtos.ReglementRequest;
import songoycollection.reglement.dtos.ReglementResponse;
import songoycollection.achat.model.Achat;
import songoycollection.achat.repository.AchatRepository;
import songoycollection.achat.repository.LigneAchatRepository;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.reglement.model.Reglement;
import songoycollection.reglement.repository.ReglementRepository;
import songoycollection.shared.EnumShared;
import songoycollection.vente.model.Vente;
import songoycollection.vente.repository.LigneVenteRepository;
import songoycollection.vente.repository.VenteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/** Centralise la création et la validation des paiements achat et vente. */
@Service
@RequiredArgsConstructor
public class ReglementService {

    private final ReglementRepository reglementRepository;
    private final AchatRepository achatRepository;
    private final LigneAchatRepository ligneAchatRepository;
    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final PersonnelUtiliShared personnelUtil;

    @Transactional
    public ReglementResponse reglerAchat(
            Long achatId,
            Long idAdmin,
            ReglementRequest request
    ) {
        Personnel personnel = validateAdmin(idAdmin);
        Achat achat = achatRepository.findById(achatId)
                .orElseThrow(() -> new IllegalArgumentException("Achat introuvable."));
        Reglement reglement = enregistrerPourAchat(
                achat,
                request.montant(),
                request.modePaiement(),
                request.date(),
                personnel
        );
        return toResponse(reglement, remainingForAchat(achat.getId()));
    }

    @Transactional
    public ReglementResponse reglerVente(
            Long venteId,
            Long idAdmin,
            ReglementRequest request
    ) {
        Personnel personnel = validateAdmin(idAdmin);
        Vente vente = venteRepository.findById(venteId)
                .orElseThrow(() -> new IllegalArgumentException("Vente introuvable."));
        Reglement reglement = enregistrerPourVente(
                vente,
                request.montant(),
                request.modePaiement(),
                request.date(),
                personnel
        );
        return toResponse(reglement, remainingForVente(vente.getId()));
    }

    @Transactional
    public Reglement enregistrerPourAchat(
            Achat achat,
            BigDecimal montant,
            EnumShared.ModePaiement modePaiement,
            LocalDateTime date,
            Personnel personnel
    ) {
        validateAchat(achat);
        BigDecimal total = money(ligneAchatRepository.sumTotalByAchatId(achat.getId()));
        BigDecimal dejaRegle = zeroIfNull(reglementRepository.sumByAchatId(achat.getId()));
        return enregistrer(
                montant,
                total.subtract(dejaRegle),
                modePaiement,
                date,
                personnel,
                achat,
                null
        );
    }

    @Transactional
    public Reglement enregistrerPourVente(
            Vente vente,
            BigDecimal montant,
            EnumShared.ModePaiement modePaiement,
            LocalDateTime date,
            Personnel personnel
    ) {
        validateVente(vente);
        BigDecimal total = money(ligneVenteRepository.sumTotalByVenteId(vente.getId()));
        BigDecimal dejaRegle = zeroIfNull(reglementRepository.sumByVenteId(vente.getId()));
        return enregistrer(
                montant,
                total.subtract(dejaRegle),
                modePaiement,
                date,
                personnel,
                null,
                vente
        );
    }

    private Reglement enregistrer(
            BigDecimal montant,
            BigDecimal reste,
            EnumShared.ModePaiement modePaiement,
            LocalDateTime date,
            Personnel personnel,
            Achat achat,
            Vente vente
    ) {
        BigDecimal paiement = normalizeAmount(montant);

        if (paiement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du règlement doit être positif.");
        }
        if (reste.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cette dette est déjà entièrement réglée.");
        }
        if (paiement.compareTo(reste) > 0) {
            throw new IllegalArgumentException("Le règlement dépasse le reste à payer.");
        }
        if (modePaiement == null || modePaiement == EnumShared.ModePaiement.CREDIT) {
            throw new IllegalArgumentException("Sélectionnez un moyen de paiement effectué.");
        }
        if (date == null || personnel == null) {
            throw new IllegalArgumentException("La date et le personnel sont obligatoires.");
        }

        Reglement reglement = new Reglement();
        reglement.setAchat(achat);
        reglement.setVente(vente);
        reglement.setMontant(paiement);
        reglement.setModePaiement(modePaiement);
        reglement.setDate(date);
        reglement.setPersonnel(personnel);
        return reglementRepository.save(reglement);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(Double amount) {
        return amount == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private Personnel validateAdmin(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        return personnelUtil.getPersonnelById(idAdmin);
    }

    private BigDecimal remainingForAchat(Long achatId) {
        BigDecimal total = money(ligneAchatRepository.sumTotalByAchatId(achatId));
        BigDecimal paid = zeroIfNull(reglementRepository.sumByAchatId(achatId));
        return total.subtract(paid).max(BigDecimal.ZERO);
    }

    private BigDecimal remainingForVente(Long venteId) {
        BigDecimal total = money(ligneVenteRepository.sumTotalByVenteId(venteId));
        BigDecimal paid = zeroIfNull(reglementRepository.sumByVenteId(venteId));
        return total.subtract(paid).max(BigDecimal.ZERO);
    }

    private ReglementResponse toResponse(Reglement reglement, BigDecimal remaining) {
        return new ReglementResponse(
                reglement.getId(),
                reglement.getAchat() == null ? null : reglement.getAchat().getId(),
                reglement.getVente() == null ? null : reglement.getVente().getId(),
                reglement.getMontant(),
                reglement.getModePaiement(),
                reglement.getDate(),
                remaining
        );
    }

    private void validateAchat(Achat achat) {
        if (achat == null || achat.getId() == null) {
            throw new IllegalArgumentException("L'achat est obligatoire.");
        }
    }

    private void validateVente(Vente vente) {
        if (vente == null || vente.getId() == null) {
            throw new IllegalArgumentException("La vente est obligatoire.");
        }
    }
}
