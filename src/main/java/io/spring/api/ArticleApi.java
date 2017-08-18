package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.service.AuthorizationService;
import io.spring.application.data.ArticleData;
import io.spring.application.ArticleQueryService;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/articles/{slug}")
public class ArticleApi {
    private ArticleQueryService articleQueryService;
    private ArticleRepository articleRepository;

    @Autowired
    public ArticleApi(ArticleQueryService articleQueryService, ArticleRepository articleRepository) {
        this.articleQueryService = articleQueryService;
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public ResponseEntity<?> article(@PathVariable("slug") String slug,
                                     @AuthenticationPrincipal User user) {
        return articleQueryService.findBySlug(slug, user)
            .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
            .orElseThrow(ResourceNotFoundException::new);
    }

    @PutMapping
    public ResponseEntity<?> updateArticle(@PathVariable("slug") String slug,
                                           @AuthenticationPrincipal User user,
                                           @Valid @RequestBody UpdateArticleParam updateArticleParam) {
        return articleRepository.findBySlug(slug).map(article -> {
            if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new NoAuthorizationException();
            }
            article.update(
                updateArticleParam.getTitle(),
                updateArticleParam.getDescription(),
                updateArticleParam.getBody());
            articleRepository.save(article);
            return ResponseEntity.ok(articleResponse(articleQueryService.findBySlug(slug, user).get()));
        }).orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping
    public ResponseEntity deleteArticle(@PathVariable("slug") String slug,
                                        @AuthenticationPrincipal User user) {
        return articleRepository.findBySlug(slug).map(article -> {
            if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new NoAuthorizationException();
            }
            articleRepository.remove(article);
            return ResponseEntity.noContent().build();
        }).orElseThrow(ResourceNotFoundException::new);
    }

    private Map<String, Object> articleResponse(ArticleData articleData) {
        return new HashMap<String, Object>() {{
            put("article", articleData);
        }};
    }
}

@Getter
@NoArgsConstructor
@JsonRootName("article")
class UpdateArticleParam {
    private String title = "";
    private String body = "";
    private String description = "";
}
