package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleData;
import io.spring.application.ArticleQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
public class ArticleFavoriteApi {
    private ArticleFavoriteRepository articleFavoriteRepository;
    private ArticleRepository articleRepository;
    private ArticleQueryService articleQueryService;

    @Autowired
    public ArticleFavoriteApi(ArticleFavoriteRepository articleFavoriteRepository,
                              ArticleRepository articleRepository,
                              ArticleQueryService articleQueryService) {
        this.articleFavoriteRepository = articleFavoriteRepository;
        this.articleRepository = articleRepository;
        this.articleQueryService = articleQueryService;
    }

    @PostMapping
    public ResponseEntity favoriteArticle(@PathVariable("slug") String slug,
                                          @AuthenticationPrincipal User user) {
        Article article = getArticle(slug);
        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return responseArticleData(articleQueryService.findBySlug(slug, user).get());
    }

    @DeleteMapping
    public ResponseEntity unfavoriteArticle(@PathVariable("slug") String slug,
                                            @AuthenticationPrincipal User user) {
        Article article = getArticle(slug);
        articleFavoriteRepository.find(article.getId(), user.getId()).ifPresent(favorite -> {
            articleFavoriteRepository.remove(favorite);
        });
        return responseArticleData(articleQueryService.findBySlug(slug, user).get());
    }

    private ResponseEntity<HashMap<String, Object>> responseArticleData(final ArticleData articleData) {
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("article", articleData);
        }});
    }

    private Article getArticle(String slug) {
        return articleRepository.findBySlug(slug).map(article -> article)
            .orElseThrow(ResourceNotFoundException::new);
    }
}
