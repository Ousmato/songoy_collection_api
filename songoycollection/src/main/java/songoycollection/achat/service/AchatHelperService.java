package songoycollection.achat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.achat.dtos.AchatHistoriqueDetailDto;
import songoycollection.achat.dtos.AchatHistoriqueDto;
import songoycollection.achat.dtos.FournisseurDto;
import songoycollection.achat.dtos.LigneAchatHistoriqueDto;
import songoycollection.achat.dtos.PaiementAchatDto;
import songoycollection.achat.dtos.PersonnelAchatDto;
import songoycollection.achat.model.Achat;
import songoycollection.achat.model.LigneAchat;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.reglement.model.Reglement;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY, readOnly = true)
public class AchatHelperService {

    private final RepositoryService repositoryService;
    private final PersonnelUtiliShared personnelUtil;

    public void validateAdmin(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        personnelUtil.getPersonnelById(idAdmin);
    }

    public List<Achat> loadAchats() {
        return repositoryService.getAchatRepository().findAllForHistory();
    }

    public Achat loadAchat(Long achatId) {
        if (achatId == null || achatId <= 0) {
            throw new IllegalArgumentException("Identifiant d'achat invalide.");
        }
        return repositoryService.getAchatRepository().findForHistoryById(achatId)
                .orElseThrow(() -> new IllegalArgumentException("Achat introuvable."));
    }

    public Map<Long, Double> loadPurchaseTotals() {
        Map<Long, Double> totals = new HashMap<>();
        repositoryService.getLigneAchatRepository().sumTotalsByAchat()
                .forEach(row -> totals.put((Long) row[0], ((Number) row[1]).doubleValue()));
        return totals;
    }

    public Map<Long, Integer> loadLineCounts() {
        Map<Long, Integer> counts = new HashMap<>();
        repositoryService.getLigneAchatRepository().countActiveLinesByAchat()
                .forEach(row -> counts.put((Long) row[0], ((Number) row[1]).intValue()));
        return counts;
    }

    public Map<Long, Double> loadPaidTotals() {
        Map<Long, Double> paid = new HashMap<>();
        repositoryService.getReglementRepository().sumByAchat()
                .forEach(row -> paid.put((Long) row[0], ((Number) row[1]).doubleValue()));
        return paid;
    }

    public double loadPurchaseTotal(Long achatId) {
        return valueOrZero(repositoryService.getLigneAchatRepository().sumTotalByAchatId(achatId));
    }

    public double loadPaidTotal(Long achatId) {
        return valueOrZero(repositoryService.getReglementRepository().sumByAchatId(achatId));
    }

    public int loadLineCount(Long achatId) {
        return repositoryService.getLigneAchatRepository().countActiveLinesByAchatId(achatId).intValue();
    }

    public AchatHistoriqueDto toSummary(
            Achat achat,
            Map<Long, Double> totals,
            Map<Long, Double> paidTotals,
            Map<Long, Integer> lineCounts
    ) {
        double total = totals.getOrDefault(achat.getId(), 0.0);
        double paid = paidTotals.getOrDefault(achat.getId(), 0.0);
        AchatHistoriqueDto dto = new AchatHistoriqueDto();
        dto.setId(achat.getId());
        dto.setReference(achat.getReference());
        dto.setDate(achat.getDate());
        dto.setDateValidation(achat.getDateValidation());
        dto.setStatut(achat.getStatut());
        dto.setEntite(achat.getEntite());
        dto.setFournisseur(supplierName(achat));
        dto.setNumeroFacture(achat.getNumeroFacture());
        dto.setTotalAchat(total);
        dto.setMontantPaye(paid);
        dto.setResteAPayer(Math.max(total - paid, 0.0));
        dto.setStatutPaiement(paymentStatus(total, paid));
        dto.setNombreLignes(lineCounts.getOrDefault(achat.getId(), 0));
        return dto;
    }

    public AchatHistoriqueDetailDto toDetail(Achat achat, AchatHistoriqueDto summary) {
        AchatHistoriqueDetailDto dto = new AchatHistoriqueDetailDto();
        dto.setId(summary.getId());
        dto.setReference(summary.getReference());
        dto.setDate(summary.getDate());
        dto.setDateValidation(summary.getDateValidation());
        dto.setStatut(summary.getStatut());
        dto.setEntite(summary.getEntite());
        dto.setFournisseur(summary.getFournisseur());
        dto.setNumeroFacture(summary.getNumeroFacture());
        dto.setTotalAchat(summary.getTotalAchat());
        dto.setMontantPaye(summary.getMontantPaye());
        dto.setResteAPayer(summary.getResteAPayer());
        dto.setStatutPaiement(summary.getStatutPaiement());
        dto.setNombreLignes(summary.getNombreLignes());
        dto.setFournisseurDetail(toSupplier(achat));
        dto.setResponsable(toPersonnel(achat.getAdminResponsable()));
        dto.setLignes(loadLines(achat.getId()));
        dto.setPaiements(loadPayments(achat.getId()));
        return dto;
    }

    private List<LigneAchatHistoriqueDto> loadLines(Long achatId) {
        return repositoryService.getLigneAchatRepository().findActiveLinesForHistory(achatId)
                .stream()
                .map(this::toLine)
                .toList();
    }

    private List<PaiementAchatDto> loadPayments(Long achatId) {
        return repositoryService.getReglementRepository().findByAchatIdOrderByDateAscIdAsc(achatId)
                .stream()
                .map(this::toPayment)
                .toList();
    }

    private double valueOrZero(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private LigneAchatHistoriqueDto toLine(LigneAchat line) {
        var variant = line.getVariant();
        var article = variant.getArticle();
        var category = article.getCategory();
        LigneAchatHistoriqueDto dto = new LigneAchatHistoriqueDto();
        dto.setId(line.getId());
        dto.setVariantId(variant.getId());
        dto.setReferenceVariant(variant.getReference());
        dto.setArticleId(article.getId());
        String categoryName = category != null ? category.getNom() : "Catégorie inconnue";
        String typeName = article.getArticleType() != null
                ? article.getArticleType().getNom()
                : "Type non renseigné";
        dto.setArticle(categoryName + " - " + typeName);
        dto.setUnite(category.getMesureCategory());
        dto.setQuantite(line.getQuantite());
        dto.setPrixAchatUnitaire(line.getPrixAchat());
        dto.setTotalLigne(line.getQuantite() * line.getPrixAchat());
        return dto;
    }

    private PaiementAchatDto toPayment(Reglement reglement) {
        PaiementAchatDto dto = new PaiementAchatDto();
        dto.setId(reglement.getId());
        dto.setDate(reglement.getDate());
        dto.setMontant(reglement.getMontant().doubleValue());
        dto.setModePaiement(reglement.getModePaiement());
        return dto;
    }

    private FournisseurDto toSupplier(Achat achat) {
        if (achat.getFournisseur() == null) {
            return null;
        }
        FournisseurDto dto = new FournisseurDto();
        dto.setId(achat.getFournisseur().getId());
        dto.setNom(achat.getFournisseur().getNom());
        dto.setPrenom(achat.getFournisseur().getPrenom());
        dto.setTelephone(achat.getFournisseur().getTelephone());
        return dto;
    }

    private PersonnelAchatDto toPersonnel(Personnel personnel) {
        if (personnel == null) {
            return null;
        }
        PersonnelAchatDto dto = new PersonnelAchatDto();
        dto.setId(personnel.getId());
        dto.setNom(personnel.getNom());
        dto.setPrenom(personnel.getPrenom());
        return dto;
    }

    private String supplierName(Achat achat) {
        if (achat.getFournisseur() != null) {
            String lastName = achat.getFournisseur().getNom();
            String firstName = achat.getFournisseur().getPrenom();
            String registeredName = Stream.of(lastName, firstName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.joining(" "));
            return registeredName.isEmpty() ? achat.getNomFournisseur() : registeredName;
        }
        return achat.getNomFournisseur();
    }

    private EnumShared.StatutPaiement paymentStatus(double total, double paid) {
        if (paid <= 0) {
            return EnumShared.StatutPaiement.CREDIT;
        }
        return paid >= total
                ? EnumShared.StatutPaiement.PAYE
                : EnumShared.StatutPaiement.PARTIEL;
    }

}
