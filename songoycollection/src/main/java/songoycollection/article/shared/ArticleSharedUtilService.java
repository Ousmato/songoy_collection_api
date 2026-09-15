package songoycollection.article.shared;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import songoycollection.article.model.Article;
import songoycollection.shared.RepositoryService;

@Service
@RequiredArgsConstructor
public class ArticleSharedUtilService {

    private final RepositoryService repo;

    public Article getArticle(Long id) {
        return repo.getArticleRepository()
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Article non trouver"));
    }
}
