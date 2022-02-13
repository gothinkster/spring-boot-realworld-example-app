package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@RequiredArgsConstructor
public class ArticleFavoriteApi {

    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final ArticleRepository articleRepository;
    private final ArticleQueryService articleQueryService;


    @PostMapping
    public ResponseEntity<?> favoriteArticle(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal User user
    ) {
        var article = articleRepository
                .findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
        var articleFavorite = new ArticleFavorite(article.getId(), user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return responseArticleData(articleQueryService.findBySlug(slug, user).get());
    }


    @DeleteMapping
    public ResponseEntity<?> unfavoriteArticle(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal User user
    ) {
        var article = articleRepository
                .findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
        articleFavoriteRepository
                .find(article.getId(), user.getId())
                .ifPresent(articleFavoriteRepository::remove);
        return responseArticleData(articleQueryService.findBySlug(slug, user).get());
    }


    private ResponseEntity<Map<String, ArticleData>> responseArticleData(ArticleData articleData) {
        return ResponseEntity.ok(Map.of("article", articleData));
    }

}
