package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.core.article.Article;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class DuplicatedArticleValidator implements ConstraintValidator<DuplicatedArticleConstraint, String> {

    @Autowired
    private ArticleQueryService articleQueryService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !articleQueryService.findBySlug(Article.toSlug(value), null).isPresent();
    }

}
