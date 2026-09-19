package songoycollection.setting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songoycollection.personne.model.Personnel;
import songoycollection.personne.shared.PersonnelUtiliShared;
import songoycollection.setting.dtos.SettingMenuStatsDto;
import songoycollection.shared.EnumShared;
import songoycollection.shared.RepositoryService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SettingMenuStatsService {

    private final RepositoryService repositoryService;
    private final PersonnelUtiliShared personnelUtil;

    @Transactional(readOnly = true)
    public SettingMenuStatsDto getStats(Long idAdmin, EnumShared.Entite entiteDemandee)
            throws AccessDeniedException {
        Personnel requester = personnelUtil.getPersonnelById(idAdmin);
        if (requester.getRole() != EnumShared.PersonnelRole.ADMIN
                && requester.getRole() != EnumShared.PersonnelRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Seul un Admin peut consulter les statistiques des paramètres.");
        }

        EnumShared.Entite entite = resolveEntite(requester.getEntite(), entiteDemandee);
        YearMonth moisCourant = YearMonth.now();
        LocalDate debutMois = moisCourant.atDay(1);
        LocalDate finMois = moisCourant.atEndOfMonth();

        SettingMenuStatsDto dto = new SettingMenuStatsDto();
        dto.setEntite(entite);
        dto.setDebutMois(debutMois);
        dto.setFinMois(finMois);
        dto.setNombreCategoriesActives(
                repositoryService.getCategoryRepository().countByActiveTrueAndEntite(entite)
        );
        dto.setNombreTypesArticlesUtilises(
                repositoryService.getArticleRepository().countDistinctActiveTypesByEntite(entite)
        );
        dto.setMontantSalairesMois(money(repositoryService.getDepenseRepository()
                .sumAmountByEntiteAndMotifBetween(
                        entite,
                        EnumShared.MotifDepense.SALAIRE,
                        debutMois.atStartOfDay(),
                        finMois.plusDays(1).atStartOfDay()
                )));
        dto.setMontantAutresDepensesMois(money(repositoryService.getDepenseRepository()
                .sumAmountByEntiteAndMotifDifferentBetween(
                        entite,
                        EnumShared.MotifDepense.SALAIRE,
                        debutMois.atStartOfDay(),
                        finMois.plusDays(1).atStartOfDay()
                )));
        return dto;
    }

    private EnumShared.Entite resolveEntite(
            EnumShared.Entite entiteUtilisateur,
            EnumShared.Entite entiteDemandee
    ) {
        if (entiteUtilisateur == EnumShared.Entite.GLOBAL) {
            if (entiteDemandee == null || entiteDemandee == EnumShared.Entite.GLOBAL) {
                throw new IllegalArgumentException("L'entité des statistiques est obligatoire.");
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

    private BigDecimal money(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }
}
