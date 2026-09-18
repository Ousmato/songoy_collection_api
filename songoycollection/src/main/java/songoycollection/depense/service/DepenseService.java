package songoycollection.depense.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.config.HttpCustumMessage;
import songoycollection.depense.dtos.DepenseHistoriqueDto;
import songoycollection.depense.dtos.DepenseRequest;
import songoycollection.shared.RepositoryService;

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
}
