package songoycollection.shared;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.achat.repository.AchatRepository;
import songoycollection.achat.repository.LigneAchatRepository;
import songoycollection.article.repository.ArticleRepository;
import songoycollection.article.repository.ArticleTypeRepository;
import songoycollection.article.repository.ArticleVariantRepository;
import songoycollection.article.repository.ArticleVariantAttributeRepository;
import songoycollection.category.repository.CategoryRepository;
import songoycollection.category.repository.CategoryAttributeRepository;
import songoycollection.category.repository.AttributeDefinitionRepository;
import songoycollection.depense.repository.DepenseRepository;
import songoycollection.personne.repository.FourniseurRepository;
import songoycollection.personne.repository.PersonnelRepository;
import songoycollection.reglement.repository.ReglementRepository;
import songoycollection.stock.repository.StockLotRepository;
import songoycollection.stock.repository.StockMouvementRepository;
import songoycollection.vente.repository.LigneVenteRepository;
import songoycollection.vente.repository.VenteRepository;

@Data
@Service
@RequiredArgsConstructor
public class RepositoryService {
    private final CategoryRepository categoryRepository;

    private final CategoryAttributeRepository categoryAttributeRepository;

    private final AttributeDefinitionRepository attributeDefinitionRepository;

    private final PersonnelRepository personnelRepository;

    private final ArticleRepository articleRepository;

    private final ArticleTypeRepository articleTypeRepository;

    private final ArticleVariantRepository articleVariantRepository;

    private final ArticleVariantAttributeRepository articleVariantAttributeRepository;

    private final StockLotRepository stockLotRepository;

    private final AchatRepository achatRepository;

    private final LigneAchatRepository ligneAchatRepository;

    private final DepenseRepository depenseRepository;

    private final ReglementRepository reglementRepository;

    private final VenteRepository venteRepository;

    private final LigneVenteRepository ligneVenteRepository;

    private final StockMouvementRepository stockMouvementRepository;

    private final FourniseurRepository fourniseurRepository;
}
