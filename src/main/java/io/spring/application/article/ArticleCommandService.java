package io.spring.application.article;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;
  private ArticleQueryService articleQueryService;


    public Article createArticle(@Valid NewArticleParam newArticleParam, User creator) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            creator.getId());
    articleRepository.save(article);
    return article;
  }

    public Article updateArticle(
            String slug,
            User user,
            @Valid UpdateArticleParam updateArticleParam) {

        Article article =
                articleRepository
                        .findBySlug(slug)
                        .orElseThrow(ResourceNotFoundException::new);

        if (!AuthorizationService.canWriteArticle(user, article)) {
            throw new NoAuthorizationException();
        }

        article.update(
                updateArticleParam.getTitle(),
                updateArticleParam.getDescription(),
                updateArticleParam.getBody());

        articleRepository.save(article);

        return article;
    }


    public void deleteArticle(String slug, User user) {

        Article article =
                articleRepository
                        .findBySlug(slug)
                        .orElseThrow(ResourceNotFoundException::new);

        if (!AuthorizationService.canWriteArticle(user, article)) {
            throw new NoAuthorizationException();
        }

        articleRepository.remove(article);
    }
}
