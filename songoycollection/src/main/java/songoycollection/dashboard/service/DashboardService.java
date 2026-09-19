package songoycollection.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.dashboard.dtos.DashboardAlerteStockDto;
import songoycollection.dashboard.dtos.DashboardIndicateursDto;
import songoycollection.dashboard.dtos.DashboardTopProduitDto;
import songoycollection.dashboard.dtos.DashboardVenteJourDto;
import songoycollection.dashboard.dtos.DashboardVenteLigneDto;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;
import songoycollection.vente.model.Vente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TOP_PRODUITS_LIMIT = 5;

    private final PersonnelUtiliShared personnelUtil;
    private final RepositoryService repositoryService;
    private final StockAlertThresholdProperties stockAlertThresholds;

    @Transactional(readOnly = true)
    public DashboardIndicateursDto getIndicateurs(
            Long idAdmin,
            EnumShared.Entite entiteDemandee,
            LocalDate dateReference,
            LocalDate debut,
            LocalDate fin
    ) {
        var personnel = personnelUtil.getPersonnelById(idAdmin);
        EnumShared.Entite entite = resolveEntite(personnel.getEntite(), entiteDemandee);

        Period period = resolvePeriod(dateReference, debut, fin);
        LocalDateTime debutPeriode = period.debut().atStartOfDay();
        LocalDateTime finPeriode = period.finExclusive().atStartOfDay();

        BigDecimal caJour = money(repositoryService.getLigneVenteRepository().sumRevenueBetween(
                entite, debutPeriode, finPeriode
        ));

        BigDecimal coutVenduMois = money(repositoryService.getStockMouvementRepository().sumCostByTypeAndDateBetween(
                EnumShared.TypeMouvement.VENTE,
                entite,
                debutPeriode,
                finPeriode
        ));
        BigDecimal valeurEntree = money(repositoryService.getStockMouvementRepository().sumCostByTypeAndDateBetween(
                EnumShared.TypeMouvement.ACHAT,
                entite,
                debutPeriode,
                finPeriode
        ));
        BigDecimal depensesMois = amount(repositoryService.getDepenseRepository().sumAmountBetween(
                entite,
                debutPeriode,
                finPeriode
        ));
        BigDecimal beneficeEstime = caJour.subtract(coutVenduMois).subtract(depensesMois);

        DashboardIndicateursDto dto = new DashboardIndicateursDto();
        dto.setCaJour(caJour);
        dto.setVentesJour(repositoryService.getVenteRepository().countActiveBetween(entite, debutPeriode, finPeriode));
        dto.setValeurEntree(valeurEntree);
        dto.setValeurSortie(coutVenduMois);
        dto.setBeneficeEstime(beneficeEstime.setScale(2, RoundingMode.HALF_UP));
        dto.setValeurStock(money(repositoryService.getStockLotRepository().sumAccountingValue(entite)));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DashboardAlerteStockDto> getAlertesStock(
            Long idAdmin,
            EnumShared.Entite entiteDemandee
    ) {
        var personnel = personnelUtil.getPersonnelById(idAdmin);
        EnumShared.Entite entite = resolveEntite(personnel.getEntite(), entiteDemandee);

        List<DashboardAlerteStockDto> alertes = new ArrayList<>();
        repositoryService.getStockLotRepository()
                .aggregateDashboardStockByVariant(entite)
                .forEach(row -> {
                    EnumShared.UniteMesureCategory unite =
                            (EnumShared.UniteMesureCategory) row[5];
                    StockAlertThresholdProperties.Thresholds seuils =
                            stockAlertThresholds.forUnit(unite);
                    double quantiteRestante = ((Number) row[6]).doubleValue();
                    String statut = stockAlertStatus(quantiteRestante, seuils);
                    if (statut == null) {
                        return;
                    }

                    DashboardAlerteStockDto dto = new DashboardAlerteStockDto();
                    dto.setVariantId((Long) row[0]);
                    dto.setArticleId((Long) row[1]);
                    String categorie = (String) row[2];
                    dto.setArticle(articleName(categorie, (String) row[3]));
                    dto.setCategorie(categorie);
                    dto.setReference((String) row[4]);
                    dto.setUnite(unite);
                    dto.setQuantiteRestante(quantiteRestante);
                    dto.setStatut(statut);
                    alertes.add(dto);
                });
        return alertes;
    }

    private String stockAlertStatus(
            double quantiteRestante,
            StockAlertThresholdProperties.Thresholds seuils
    ) {
        if (quantiteRestante <= 0) {
            return "rupture";
        }
        if (quantiteRestante <= seuils.critique()) {
            return "critique";
        }
        if (quantiteRestante <= seuils.alerte()) {
            return "alerte";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<DashboardTopProduitDto> getTopProduits(
            Long idAdmin,
            EnumShared.Entite entiteDemandee,
            LocalDate dateReference,
            LocalDate debut,
            LocalDate fin
    ) {
        var personnel = personnelUtil.getPersonnelById(idAdmin);
        EnumShared.Entite entite = resolveEntite(personnel.getEntite(), entiteDemandee);

        Period period = resolvePeriod(dateReference, debut, fin);
        LocalDateTime debutPeriode = period.debut().atStartOfDay();
        LocalDateTime finPeriode = period.finExclusive().atStartOfDay();

        List<Object[]> aggregates = repositoryService.getLigneVenteRepository()
                .aggregateDashboardTopProducts(entite, debutPeriode, finPeriode);
        if (aggregates.isEmpty()) {
            return List.of();
        }

        List<Object[]> topAggregates = aggregates.stream()
                .limit(TOP_PRODUITS_LIMIT)
                .toList();
        BigDecimal totalTopRevenue = topAggregates.stream()
                .map(row -> money(((Number) row[5]).doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardTopProduitDto> result = new ArrayList<>(topAggregates.size());
        for (int index = 0; index < topAggregates.size(); index++) {
            Object[] row = topAggregates.get(index);
            double quantity = ((Number) row[4]).doubleValue();
            BigDecimal revenue = money(((Number) row[5]).doubleValue());

            DashboardTopProduitDto dto = new DashboardTopProduitDto();
            dto.setArticleId((Long) row[0]);
            String categorie = (String) row[1];
            dto.setArticle(articleName(categorie, (String) row[2]));
            dto.setCategorie(categorie);
            dto.setUnite((EnumShared.UniteMesureCategory) row[3]);
            dto.setQuantiteVendue(quantity);
            dto.setChiffreAffaires(revenue);
            dto.setPrixMoyen(quantity == 0.0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : revenue.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP));
            dto.setRang(index + 1);
            dto.setPartChiffreAffaires(totalTopRevenue.signum() == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : revenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalTopRevenue, 2, RoundingMode.HALF_UP));
            result.add(dto);
        }
        return result;
    }

    private String articleName(String categorie, String type) {
        if (categorie == null || categorie.isBlank()) {
            return type == null || type.isBlank() ? "Article" : type;
        }
        if (type == null || type.isBlank()) {
            return categorie;
        }
        return categorie + " - " + type;
    }

    @Transactional(readOnly = true)
    public List<DashboardVenteJourDto> getVentesDuJour(
            Long idAdmin,
            EnumShared.Entite entiteDemandee,
            LocalDate dateReference
    ) {
        var personnel = personnelUtil.getPersonnelById(idAdmin);
        EnumShared.Entite entite = resolveEntite(personnel.getEntite(), entiteDemandee);
        LocalDate date = dateReference == null ? LocalDate.now() : dateReference;
        LocalDateTime debut = date.atStartOfDay();
        LocalDateTime finExclusive = date.plusDays(1).atStartOfDay();

        List<Vente> ventes = repositoryService.getVenteRepository()
                .findDashboardSalesBetween(entite, debut, finExclusive);
        if (ventes.isEmpty()) {
            return List.of();
        }

        List<Long> venteIds = ventes.stream().map(songoycollection.vente.model.Vente::getId).toList();
        Map<Long, Object[]> summaries = new HashMap<>();
        repositoryService.getLigneVenteRepository()
                .summarizeDashboardSales(entite, venteIds)
                .forEach(row -> summaries.put((Long) row[0], row));
        Map<Long, List<DashboardVenteLigneDto>> linesBySale = new HashMap<>();
        repositoryService.getLigneVenteRepository()
                .findDashboardSaleLines(entite, venteIds)
                .forEach(row -> {
                    DashboardVenteLigneDto line = new DashboardVenteLigneDto();
                    line.setReference((String) row[1]);
                    line.setQuantite(((Number) row[2]).doubleValue());
                    line.setUnite((EnumShared.UniteMesureCategory) row[3]);
                    linesBySale.computeIfAbsent((Long) row[0], ignored -> new java.util.ArrayList<>()).add(line);
                });

        return ventes.stream().map(vente -> {
            Object[] summary = summaries.get(vente.getId());
            DashboardVenteJourDto dto = new DashboardVenteJourDto();
            dto.setId(vente.getId());
            dto.setLignes(linesBySale.getOrDefault(vente.getId(), List.of()));
            dto.setClient(clientName(vente));
            dto.setDate(vente.getDate());
            dto.setModePaiement(vente.getModePaiement());
            dto.setQuantiteArticles(summary == null ? 0.0 : ((Number) summary[1]).doubleValue());
            dto.setTotal(money(summary == null ? 0.0 : ((Number) summary[2]).doubleValue()));
            return dto;
        }).toList();
    }

    private String clientName(songoycollection.vente.model.Vente vente) {
        if (vente.getClient() != null) {
            return ((vente.getClient().getPrenom() == null ? "" : vente.getClient().getPrenom()) + " "
                    + (vente.getClient().getNom() == null ? "" : vente.getClient().getNom())).trim();
        }
        return vente.getNomClient() == null || vente.getNomClient().isBlank()
                ? "Client passager"
                : vente.getNomClient();
    }

    private Period resolvePeriod(LocalDate dateReference, LocalDate debut, LocalDate fin) {
        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException("Les deux dates de la période sont obligatoires.");
        }
        LocalDate start = debut != null ? debut : (dateReference == null ? LocalDate.now() : dateReference);
        LocalDate end = fin != null ? fin : start;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("La date de début doit être antérieure ou égale à la date de fin.");
        }
        return new Period(start, end.plusDays(1));
    }

    private record Period(LocalDate debut, LocalDate finExclusive) {}

    private EnumShared.Entite resolveEntite(
            EnumShared.Entite entiteUtilisateur,
            EnumShared.Entite entiteDemandee
    ) {
        if (entiteUtilisateur == EnumShared.Entite.GLOBAL) {
            if (entiteDemandee == null || entiteDemandee == EnumShared.Entite.GLOBAL) {
                throw new IllegalArgumentException("L'entité du dashboard est obligatoire.");
            }
            return entiteDemandee;
        }

        if (entiteUtilisateur == null) {
            throw new IllegalArgumentException("L'utilisateur n'a pas d'entité définie.");
        }
        if (entiteDemandee != null && entiteDemandee != entiteUtilisateur) {
            throw new IllegalArgumentException("L'utilisateur n'est pas autorisé pour cette entité.");
        }
        return entiteUtilisateur;
    }

    private BigDecimal money(Double amount) {
        return BigDecimal.valueOf(amount == null ? 0.0 : amount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal amount(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
