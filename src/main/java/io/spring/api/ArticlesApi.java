package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(path = "/articles")
@RequiredArgsConstructor
public class ArticlesApi {

    private final ArticleCommandService articleCommandService;
    private final ArticleQueryService articleQueryService;


    @PostMapping
    public ResponseEntity<?> createArticle(
            @Valid @RequestBody NewArticleParam newArticleParam,
            @AuthenticationPrincipal User user
    ) {
        var article = articleCommandService.createArticle(newArticleParam, user);
        return ResponseEntity.ok(Map.of("article", articleQueryService.findById(article.getId(), user).get()));
    }


    @GetMapping(path = "feed")
    public ResponseEntity<?> getFeed(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
    }


    @GetMapping
    public ResponseEntity<?> getArticles(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "favorited", required = false) String favoritedBy,
            @RequestParam(value = "author", required = false) String author,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(articleQueryService.findRecentArticles(
                tag,
                author,
                favoritedBy,
                new Page(offset, limit),
                user
        ));
    }

}
