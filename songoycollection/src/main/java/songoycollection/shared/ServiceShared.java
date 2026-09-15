package songoycollection.shared;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.achat.service.AchatService;
import songoycollection.article.service.ArticleService;
import songoycollection.category.service.CategoryService;
import songoycollection.personne.service.AdminService;

@Data
@RequiredArgsConstructor
@Service
public class ServiceShared {

    private final CategoryService categoryService;

    private final AdminService adminService;

    private final ArticleService articleService;

    private final AchatService achatService;
}
