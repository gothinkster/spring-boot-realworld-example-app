package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Service
@Validated
@RequiredArgsConstructor
public class ArticleCommandService {

    private final ArticleRepository articleRepository;


    public Article createArticle(
            @Valid NewArticleParam newArticleParam,
            User creator
    ) {
        var article = new Article(
                newArticleParam.getTitle(),
                newArticleParam.getDescription(),
                newArticleParam.getBody(),
                newArticleParam.getTagList(),
                creator.getId());
        articleRepository.save(article);
        return article;
    }


    public Article updateArticle(
            Article article,
            @Valid UpdateArticleParam updateArticleParam
    ) {
        article.update(
                updateArticleParam.getTitle(),
                updateArticleParam.getDescription(),
                updateArticleParam.getBody()
        );
        articleRepository.save(article);
        return article;
    }

}
