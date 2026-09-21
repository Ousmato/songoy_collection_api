package songoycollection.depense.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.config.HttpCustumMessage;
import songoycollection.depense.dtos.DepenseHistoriqueDto;
import songoycollection.depense.dtos.DepenseRequest;
import songoycollection.depense.dtos.SalaireHistoriqueDto;
import songoycollection.depense.dtos.SalaireStatistiquesDto;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepenseService {

    private final RepositoryService repositoryService;
    private final DepenseHelperService helper;

    @Transactional
    public HttpCustumMessage addDepense(DepenseRequest request, Long idAdmin) {
        helper.validateRequest(request);
        var responsable = helper.loadResponsible(idAdmin);
        var beneficiaire = helper.resolveBeneficiary(request);
        var fournisseur = helper.resolveFournisseur(request);
        var depense = helper.createEntity(request, responsable, beneficiaire, fournisseur);
        repositoryService.getDepenseRepository().save(depense);
        return HttpCustumMessage.addMessage();
    }

    @Transactional(readOnly = true)
    public List<DepenseHistoriqueDto> getDepenses(
            Long idAdmin,
            LocalDate debut,
            LocalDate fin
    ) {
        helper.validateAdmin(idAdmin);

        LocalDate aujourdHui = LocalDate.now();
        LocalDate dateDebut = debut == null
                ? aujourdHui.withDayOfMonth(1)
                : debut;
        LocalDate dateFin = fin == null ? aujourdHui : fin;

        if (dateDebut.isAfter(dateFin)) {
            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin."
            );
        }
        if (dateFin.isAfter(aujourdHui)) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas être future."
            );
        }

        return repositoryService.getDepenseRepository()
                .findByDateGreaterThanEqualAndDateLessThanOrderByDateDescIdDesc(
                        dateDebut.atStartOfDay(),
                        dateFin.plusDays(1).atStartOfDay()
                )
                .stream()
                .map(helper::toHistoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalaireHistoriqueDto> getHistoriqueSalaires(
            Long idAdmin,
            EnumShared.Entite entite,
            String periodeSalaire
    ) {
        helper.validateAdmin(idAdmin);
        if (periodeSalaire != null && !periodeSalaire.isBlank()) {
            helper.validateSalaryPeriod(periodeSalaire);
        }

        String periodFilter = periodeSalaire == null || periodeSalaire.isBlank()
                ? null
                : periodeSalaire.trim();
        return repositoryService.getDepenseRepository().findHistoriqueSalaires(
                        EnumShared.MotifDepense.SALAIRE,
                        entite,
                        periodFilter
                )
                .stream()
                .map(helper::toSalaryHistoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalaireStatistiquesDto getStatistiquesSalaires(
            Long idAdmin,
            EnumShared.Entite entite,
            String periodeSalaire
    ) {
        helper.validateAdmin(idAdmin);
        helper.validateSalaryPeriod(periodeSalaire);

        String periode = periodeSalaire.trim();
        List<EnumShared.PersonnelRole> rolesExclus = List.of(
                EnumShared.PersonnelRole.CLIENT,
                EnumShared.PersonnelRole.ADMIN,
                EnumShared.PersonnelRole.SUPER_ADMIN
        );
        List<EnumShared.Entite> entites = entite == null
                ? List.of(EnumShared.Entite.values())
                : List.of(entite);

        long nombrePersonnesPayees = repositoryService.getDepenseRepository()
                .countBeneficiairesPayesParPeriode(
                        EnumShared.MotifDepense.SALAIRE,
                        periode,
                        entite,
                        rolesExclus
                );
        long nombrePersonnelConcerne = repositoryService.getPersonnelRepository()
                .countByEntiteInAndActiveTrueAndRoleNotIn(entites, rolesExclus);

        SalaireStatistiquesDto dto = new SalaireStatistiquesDto();
        dto.setPeriodeSalaire(periode);
        dto.setEntite(entite);
        dto.setMontantVerse(money(repositoryService.getDepenseRepository()
                .sumMontantSalairesParPeriode(
                        EnumShared.MotifDepense.SALAIRE,
                        periode,
                        entite
                )));
        dto.setNombrePersonnesPayees(nombrePersonnesPayees);
        dto.setNombrePersonnesNonPayees(Math.max(0, nombrePersonnelConcerne - nombrePersonnesPayees));
        return dto;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
