package io.spring.application;

import io.spring.core.article.Article;
import io.spring.core.user.User;

public class AuthorizationService {
    public static boolean canWriteArticle(User user, Article article) {
        return user.getId().equals(article.getUserId());
    }
}
