package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.stream.Collectors;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ArticleFavoriteApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleFavoriteApiTest extends TestWithCurrentUser {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArticleFavoriteRepository articleFavoriteRepository;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleQueryService articleQueryService;

    private Article article;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
        User anotherUser = new User("other@test.com", "other", "123", "", "");
        article = new Article("title", "desc", "body", new String[]{"java"}, anotherUser.getId());
        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
        ArticleData articleData = new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            true,
            1,
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
            new ProfileData(
                anotherUser.getId(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage(),
                false
            ));
        when(articleQueryService.findBySlug(eq(articleData.getSlug()), eq(user))).thenReturn(Optional.of(articleData));
    }

    @Test
    public void should_favorite_an_article_success() throws Exception {
        given()
            .header("Authorization", "Token " + token)
            .when()
            .post("/articles/{slug}/favorite", article.getSlug())
            .prettyPeek()
            .then()
            .statusCode(200)
            .body("article.id", equalTo(article.getId()));

        verify(articleFavoriteRepository).save(any());
    }

    @Test
    public void should_unfavorite_an_article_success() throws Exception {
        when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId()))).thenReturn(Optional.of(new ArticleFavorite(article.getId(), user.getId())));
        given()
            .header("Authorization", "Token " + token)
            .when()
            .delete("/articles/{slug}/favorite", article.getSlug())
            .prettyPeek()
            .then()
            .statusCode(200)
            .body("article.id", equalTo(article.getId()));
        verify(articleFavoriteRepository).remove(new ArticleFavorite(article.getId(), user.getId()));
    }
}
