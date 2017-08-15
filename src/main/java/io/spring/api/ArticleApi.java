package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleData;
import io.spring.application.article.ArticleQueryService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}")
public class ArticleApi {
    private ArticleQueryService articleQueryService;

    @Autowired
    public ArticleApi(ArticleQueryService articleQueryService) {
        this.articleQueryService = articleQueryService;
    }

    @GetMapping
    public ResponseEntity<ArticleData> article(@PathVariable("slug") String slug,
                                               @AuthenticationPrincipal User user) {
        return articleQueryService.findBySlug(slug, user).map(ResponseEntity::ok).orElseThrow(ResourceNotFoundException::new);
    }
}
