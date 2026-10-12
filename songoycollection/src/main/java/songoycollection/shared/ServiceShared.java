package songoycollection.shared;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.achat.service.AchatService;
import songoycollection.article.service.ArticleService;
import songoycollection.category.service.CategoryService;
import songoycollection.depense.service.DepenseService;
import songoycollection.achat.service.ReceptionService;
import songoycollection.personne.service.AdminService;
import songoycollection.personne.service.FournisseurService;
import songoycollection.setting.service.SettingMenuStatsService;
import songoycollection.vente.service.VenteService;
import songoycollection.atelier.services.CommandeConfectionService;

@Data
@RequiredArgsConstructor
@Service
public class ServiceShared {

    private final CategoryService categoryService;

    private final AdminService adminService;

    private final ArticleService articleService;

    private final AchatService achatService;

    private final VenteService venteService;

    private final DepenseService depenseService;

    private final ReceptionService receptionService;

    private final FournisseurService fournisseurService;

    private final CommandeConfectionService commandeConfectionService;

    private final SettingMenuStatsService settingMenuStatsService;
}
