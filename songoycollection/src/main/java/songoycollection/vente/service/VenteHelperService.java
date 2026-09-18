package songoycollection.vente.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.article.model.ArticleVariant;
import songoycollection.article.model.ArticleVariantAttribute;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.reglement.model.Reglement;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;
import songoycollection.stock.model.StockLot;
import songoycollection.stock.model.StockMouvement;
import songoycollection.vente.dtos.LigneVenteHistorique;
import songoycollection.vente.dtos.PaiementVenteHistorique;
import songoycollection.vente.dtos.VenteLigneRequestDto;
import songoycollection.vente.dtos.VenteRequestDto;
import songoycollection.vente.model.LigneVente;
import songoycollection.vente.model.Vente;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class VenteHelperService {

    private final EntityManager entityManager;
    private final ArticleVariantRepository variantRepository;
    private final RepositoryService repositoryService;
    private final PersonnelUtiliShared personnelUtil;

    public void validateAdmin(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        personnelUtil.getPersonnelById(idAdmin);
    }

    public List<Vente> loadVentesForHistory() {
        return repositoryService.getVenteRepository().findAllForHistory();
    }

    public Vente loadVenteForHistoryDetail(Long venteId) {
        if (venteId == null || venteId <= 0) {
            throw new IllegalArgumentException("Identifiant de vente invalide.");
        }
        return repositoryService.getVenteRepository().findForHistoryById(venteId)
                .orElseThrow(() -> new IllegalArgumentException("Vente introuvable."));
    }

    public double loadSaleTotal(Long venteId) {
        return valueOrZero(repositoryService.getLigneVenteRepository().sumTotalByVenteId(venteId));
    }

    public double loadPaidTotal(Long venteId) {
        return valueOrZero(repositoryService.getReglementRepository().sumByVenteId(venteId));
    }

    public List<LigneVenteHistorique> loadHistoryLines(Long venteId) {
        return repositoryService.getLigneVenteRepository()
                .findActiveLinesForHistoryByVenteId(venteId)
                .stream()
                .map(this::toHistoryLine)
                .toList();
    }

    public List<PaiementVenteHistorique> loadHistoryPayments(Long venteId) {
        return repositoryService.getReglementRepository()
                .findByVenteIdOrderByDateAscIdAsc(venteId)
                .stream()
                .map(this::toHistoryPayment)
                .toList();
    }

    private LigneVenteHistorique toHistoryLine(LigneVente line) {
        ArticleVariant variant = line.getVariant();
        var article = variant.getArticle();
        var category = article.getCategory();

        LigneVenteHistorique dto = new LigneVenteHistorique();
        dto.setId(line.getId());
        dto.setVariantId(variant.getId());
        dto.setReferenceVariant(variant.getReference());
        dto.setArticleId(article.getId());
        dto.setArticle(articleName(article));
        dto.setUnite(category == null ? null : category.getMesureCategory());
        dto.setAttributs(new HashMap<>());
        for (ArticleVariantAttribute attribute : variant.getAttributs()) {
            if (attribute.getAttribute() != null && attribute.getAttribute().getId() != null) {
                dto.getAttributs().put(attribute.getAttribute().getId(), attribute.getValue());
            }
        }
        dto.setQuantite(line.getQuantite());
        dto.setPrixVente(line.getPrixVente() == null
                ? null
                : java.math.BigDecimal.valueOf(line.getPrixVente()));
        dto.setTotalLigne(line.getQuantite() == null || line.getPrixVente() == null
                ? 0.0
                : line.getQuantite() * line.getPrixVente());
        return dto;
    }

    private PaiementVenteHistorique toHistoryPayment(Reglement reglement) {
        PaiementVenteHistorique dto = new PaiementVenteHistorique();
        dto.setId(reglement.getId());
        dto.setDate(reglement.getDate());
        dto.setMontant(reglement.getMontant());
        dto.setModePaiement(reglement.getModePaiement());
        return dto;
    }

    private String articleName(songoycollection.article.model.Article article) {
        String categoryName = article.getCategory() == null || article.getCategory().getNom() == null
                ? "Catégorie inconnue"
                : article.getCategory().getNom();
        String typeName = article.getArticleType() == null || article.getArticleType().getNom() == null
                ? "Type non renseigné"
                : article.getArticleType().getNom();
        return categoryName + " - " + typeName;
    }

    public Map<Long, Double> loadSaleTotals() {
        Map<Long, Double> totals = new HashMap<>();
        repositoryService.getLigneVenteRepository().sumTotalsByVente()
                .forEach(row -> totals.put((Long) row[0], valueOrZero((Number) row[1])));
        return totals;
    }

    public Map<Long, Double> loadPaidTotals() {
        Map<Long, Double> paid = new HashMap<>();
        repositoryService.getReglementRepository().sumByVente()
                .forEach(row -> paid.put((Long) row[0], valueOrZero((Number) row[1])));
        return paid;
    }

    private double valueOrZero(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    public Personnel validatePersonnel(Long idAdmin) {
        personnelUtil.getPersonnelRoleIsActiveById(idAdmin, EnumShared.PersonnelRole.ADMIN);
        Personnel personnel = personnelUtil.getPersonnelById(idAdmin);
        entityManager.lock(personnel, LockModeType.PESSIMISTIC_WRITE);
        return personnel;
    }

    public void validateRequest(VenteRequestDto request) {
        if (request == null || request.getDate() == null
                || request.getModePaiement() == null
                || request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new IllegalArgumentException(
                    "La date, le mode de paiement et les lignes sont obligatoires."
            );
        }

        if (request.getClientId() != null
                && request.getClientNom() != null
                && !request.getClientNom().isBlank()) {
            throw new IllegalArgumentException(
                    "Choisissez un client enregistré ou saisissez un nom de client."
            );
        }

        Set<Long> variantIds = new HashSet<>();
        for (VenteLigneRequestDto line : request.getLignes()) {
            if (line == null || line.getVariantId() == null
                    || line.getQuantite() == null
                    || !Double.isFinite(line.getQuantite())
                    || line.getQuantite() <= 0
                    || line.getPrixVente() == null
                    || line.getPrixVente() <= 0) {
                throw new IllegalArgumentException(
                        "Chaque ligne doit contenir une variante, une quantité et un prix positifs."
                );
            }
            if (!variantIds.add(line.getVariantId())) {
                throw new IllegalArgumentException("Une variante ne peut apparaître qu'une seule fois.");
            }
        }
    }

    public Personnel resolveClient(VenteRequestDto request) {
        if (request.getClientId() == null) {
            return null;
        }

        Personnel client = personnelUtil.getPersonnelById(request.getClientId());
        if (client.getRole() != EnumShared.PersonnelRole.CLIENT) {
            throw new IllegalArgumentException("La personne sélectionnée n'est pas un client.");
        }
        return client;
    }

    public Map<Long, ArticleVariant> loadVariants(VenteRequestDto request) {
        Map<Long, ArticleVariant> variants = new HashMap<>();

        for (Long variantId : request.getLignes().stream()
                .map(VenteLigneRequestDto::getVariantId)
                .sorted()
                .toList()) {
            ArticleVariant variant = variantRepository.findForStockUpdate(variantId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Variante introuvable : " + variantId
                    ));

            if (variant.getArticle() == null
                    || !variant.getArticle().isActive()
                    || variant.getArticle().getCategory() == null
                    || !variant.getArticle().getCategory().isActive()) {
                throw new IllegalArgumentException("L'article ou sa catégorie est inactif.");
            }

            if (variant.getArticle().getCategory().getEntite() == EnumShared.Entite.ATELIER) {
                throw new IllegalArgumentException(
                        "Une vente boutique ne peut pas utiliser une variante de l'atelier."
                );
            }
            variants.put(variantId, variant);
        }
        return variants;
    }

    public double calculateTotal(VenteRequestDto request) {
        double total = 0;
        for (VenteLigneRequestDto line : request.getLignes()) {
            double lineTotal = line.getQuantite() * line.getPrixVente();
            if (!Double.isFinite(lineTotal)
                    || lineTotal <= 0
                    || lineTotal > 9_007_199_254_740_991d) {
                throw new IllegalArgumentException("Le montant de la vente est invalide.");
            }
            total += lineTotal;
        }
        if (!Double.isFinite(total) || total <= 0 || total > 9_007_199_254_740_991d) {
            throw new IllegalArgumentException("Le montant de la vente est invalide.");
        }
        return total;
    }

    public double resolvePaidAmount(VenteRequestDto request, double total) {
        Double requestedAmount = request.getMontantPaye();
        if (requestedAmount == null) {
            return request.getModePaiement() == EnumShared.ModePaiement.CREDIT
                    ? 0
                    : total;
        }

        if (!Double.isFinite(requestedAmount)
                || requestedAmount < 0
                || requestedAmount > total) {
            throw new IllegalArgumentException(
                    "Le montant payé doit être compris entre zéro et le total."
            );
        }

        if (requestedAmount > 0
                && request.getModePaiement() == EnumShared.ModePaiement.CREDIT) {
            throw new IllegalArgumentException(
                    "Un paiement doit utiliser un moyen de paiement réel."
            );
        }
        return requestedAmount;
    }

    public Vente createVente(VenteRequestDto request, Personnel caissier, Personnel client) {
        Vente vente = new Vente();
        vente.setDate(resolveVenteDate(request.getDate()));
        vente.setCaissier(caissier);
        vente.setClient(client);
        vente.setNomClient(client == null ? trimToNull(request.getClientNom()) : null);
        vente.setModePaiement(request.getModePaiement());
        entityManager.persist(vente);
        entityManager.flush();
        vente.setNumeroFacture(generateNumeroFacture(vente.getId()));
        return vente;
    }

    /**
     * Le formulaire envoie actuellement une date sans heure. Pour une vente
     * créée aujourd'hui, on conserve donc l'heure réelle du serveur ; pour une
     * vente historique, la date reste positionnée au début de la journée.
     */
    private LocalDateTime resolveVenteDate(java.time.LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        return date.equals(now.toLocalDate()) ? now : date.atStartOfDay();
    }

    private String generateNumeroFacture(Long venteId) {
        if (venteId == null) {
            throw new IllegalStateException("Impossible de générer le numéro de facture.");
        }
        return "FACN°-" + Year.now().getValue() + "-"
                + String.format(Locale.ROOT, "%03d", venteId);
    }

    public List<LigneVente> createLines(
            Vente vente,
            VenteRequestDto request,
            Map<Long, ArticleVariant> variants
    ) {
        List<LigneVente> lines = new ArrayList<>();
        for (VenteLigneRequestDto input : request.getLignes()) {
            LigneVente line = new LigneVente();
            line.setVente(vente);
            line.setVariant(variants.get(input.getVariantId()));
            line.setQuantite(input.getQuantite());
            line.setPrixVente(input.getPrixVente());
            line.setDate(vente.getDate());
            entityManager.persist(line);
            lines.add(line);
        }
        entityManager.flush();
        return lines;
    }

    public void removeFromStock(Vente vente, List<LigneVente> lines) {
        for (LigneVente line : lines) {
            double remaining = line.getQuantite();
            List<StockLot> lots = repositoryService.getStockLotRepository()
                    .findAvailableByVariantIdForUpdate(line.getVariant().getId());

            for (StockLot lot : lots) {
                if (remaining <= 0) {
                    break;
                }
                double available = lot.getQuantityRestante() == null
                        ? 0
                        : lot.getQuantityRestante();
                double consumed = Math.min(available, remaining);
                lot.setQuantityRestante(available - consumed);
                remaining -= consumed;
                createMovement(vente, line, lot, consumed);
            }

            if (remaining > 0.000000001d) {
                throw new IllegalArgumentException(
                        "Stock insuffisant pour la variante " + line.getVariant().getReference() + "."
                );
            }
        }
    }

    private void createMovement(
            Vente vente,
            LigneVente line,
            StockLot lot,
            double quantity
    ) {
        StockMouvement movement = new StockMouvement();
        movement.setStockLot(lot);
        movement.setVariant(line.getVariant());
        movement.setLigneVente(line);
        movement.setQuantite(quantity);
        movement.setType(EnumShared.TypeMouvement.VENTE);
        movement.setReference("VNT-" + vente.getId());
        movement.setDateEntree(vente.getDate());
        entityManager.persist(movement);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
