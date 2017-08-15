package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.article.ArticleData;
import io.spring.application.article.ArticleQueryService;
import io.spring.application.profile.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ArticlesApiTest extends TestWithCurrentUser {
    @LocalServerPort
    private int port;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleQueryService articleQueryService;

    protected String email;
    protected String username;
    protected String defaultAvatar;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        email = "john@jacob.com";
        username = "johnjacob";
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
        userFixture(email, username, defaultAvatar);
    }

    @Test
    public void should_create_article_success() throws Exception {
        String title = "How to train your dragon";
        String slug = "how-to-train-your-dragon";
        String description = "Ever wonder how?";
        String body = "You have to believe";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        Map<String, Object> param = prepareParam(title, description, body, tagList);

        when(articleRepository.toSlug(eq(title))).thenReturn(slug);

        ArticleData articleData = new ArticleData(
            "123",
            slug,
            title,
            description,
            body,
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList(tagList),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

        when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles")
            .then()
            .statusCode(200)
            .body("article.title", equalTo(title))
            .body("article.favorited", equalTo(false))
            .body("article.body", equalTo(body))
            .body("article.favoritesCount", equalTo(0))
            .body("article.author.username", equalTo(user.getUsername()))
            .body("article.author.id", equalTo(null));

        verify(articleRepository).save(any());
    }

    @Test
    public void should_get_error_message_with_wrong_parameter() throws Exception {
        String title = "How to train your dragon";
        String slug = "how-to-train-your-dragon";
        String description = "Ever wonder how?";
        String body = "";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        Map<String, Object> param = prepareParam(title, description, body, tagList);

        when(articleRepository.toSlug(eq(title))).thenReturn(slug);

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles")
            .then()
            .statusCode(422)
            .body("errors.body[0]", equalTo("can't be empty"));

    }

    private HashMap<String, Object> prepareParam(final String title, final String description, final String body, final String[] tagList) {
        return new HashMap<String, Object>() {{
            put("article", new HashMap<String, Object>() {{
                put("title", title);
                put("description", description);
                put("body", body);
                put("tagList", tagList);
            }});
        }};
    }

    @Test
    public void should_read_article_success() throws Exception {
        String slug = "test-new-article";
        Article article = new Article(slug, "Test New Article", "Desc", "Body", new String[]{"java", "spring", "jpg"}, user.getId());

        DateTime time = new DateTime();
        ArticleData articleData = new ArticleData(
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

        when(articleQueryService.findBySlug(eq(slug), eq(null))).thenReturn(Optional.of(articleData));

        RestAssured.when()
            .get("/articles/{slug}", slug)
            .then()
            .statusCode(200)
            .body("article.slug", equalTo(slug))
            .body("article.body", equalTo(articleData.getBody()))
            .body("article.createdAt", equalTo(time.toDateTimeISO().toString()));

    }

    @Test
    public void should_404_if_article_not_found() throws Exception {
        when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.empty());
        RestAssured.when()
            .get("/articles/not-exists")
            .then()
            .statusCode(404);
    }
}