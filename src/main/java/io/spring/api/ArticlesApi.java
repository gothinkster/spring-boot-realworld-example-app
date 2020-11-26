package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidRequestException;
import io.spring.application.Page;
import io.spring.application.ArticleQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping(path = "/articles")
public class ArticlesApi {
    private ArticleRepository articleRepository;
    private ArticleQueryService articleQueryService;

    @Autowired
    public ArticlesApi(ArticleRepository articleRepository, ArticleQueryService articleQueryService) {
        this.articleRepository = articleRepository;
        this.articleQueryService = articleQueryService;
    }

    @PostMapping
    public ResponseEntity createArticle(@Valid @RequestBody NewArticleParam newArticleParam,
                                        BindingResult bindingResult,
                                        @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        if (articleQueryService.findBySlug(Article.toSlug(newArticleParam.getTitle()), null).isPresent()) {
            bindingResult.rejectValue("title", "DUPLICATED", "article name exists");
            throw new InvalidRequestException(bindingResult);
        }

        Article article = new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            user.getId());
        articleRepository.save(article);
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("article", articleQueryService.findById(article.getId(), user).get());
        }});
    }

    @GetMapping(path = "feed")
    public ResponseEntity getFeed(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                  @RequestParam(value = "limit", defaultValue = "20") int limit,
                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
    }

    @GetMapping
    public ResponseEntity getArticles(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "limit", defaultValue = "20") int limit,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "favorited", required = false) String favoritedBy,
                                      @RequestParam(value = "author", required = false) String author,
                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(articleQueryService.findRecentArticles(tag, author, favoritedBy, new Page(offset, limit), user));
    }
}

@Getter
@JsonRootName("article")
@NoArgsConstructor
class NewArticleParam {
    @NotBlank(message = "can't be empty")
    private String title;
    @NotBlank(message = "can't be empty")
    private String description;
    @NotBlank(message = "can't be empty")
    private String body;
    private String[] tagList;
}