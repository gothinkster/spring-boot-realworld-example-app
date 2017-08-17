package io.spring;

import io.spring.application.article.ArticleData;
import io.spring.application.profile.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;

public class TestHelper {
    public static ArticleData articleDataFixture(String seed, User user) {
        DateTime now = new DateTime();
        return new ArticleData(
            seed + "id",
            "title-" + seed,
            "title " + seed,
            "desc " + seed,
            "body " + seed, false, 0, now, now, new ArrayList<>(),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    }

    public static ArticleData getArticleDataFromArticleAndUser(Article article, User user) {
        DateTime time = new DateTime();
        return new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            time,
            time,
            Arrays.asList("joda"),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    }
}
