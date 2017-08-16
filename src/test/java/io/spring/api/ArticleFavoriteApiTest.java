package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.article.ArticleData;
import io.spring.application.article.ArticleQueryService;
import io.spring.application.article.ArticleReadService;
import io.spring.application.profile.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ArticleFavoriteApiTest extends TestWithCurrentUser {
    @MockBean
    private ArticleFavoriteRepository articleFavoriteRepository;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleQueryService articleQueryService;

    protected String email;
    protected String username;
    protected String defaultAvatar;

    @LocalServerPort
    private int port;
    private Article article;
    private User anotherUser;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        email = "john@jacob.com";
        username = "johnjacob";
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
        userFixture(email, username, defaultAvatar);
        anotherUser = new User("other@test.com", "other", "123", "", "");
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
            .statusCode(201)
            .body("article.id", equalTo(article.getId()));
    }
}
