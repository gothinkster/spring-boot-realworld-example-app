package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleQueryService articleQueryService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    public void should_create_article_success() throws Exception {
        String title = "How to train your dragon";
        String slug = "how-to-train-your-dragon";
        String description = "Ever wonder how?";
        String body = "You have to believe";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        Map<String, Object> param = prepareParam(title, description, body, tagList);

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

        when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any())).thenReturn(Optional.empty());

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
        String description = "Ever wonder how?";
        String body = "";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        Map<String, Object> param = prepareParam(title, description, body, tagList);

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles")
            .prettyPeek()
            .then()
            .statusCode(422)
            .body("errors.body[0]", equalTo("can't be empty"));

    }

    @Test
    public void should_get_error_message_with_duplicated_title() {
        String title = "How to train your dragon";
        String slug = "how-to-train-your-dragon";
        String description = "Ever wonder how?";
        String body = "You have to believe";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        Map<String, Object> param = prepareParam(title, description, body, tagList);

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

        when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any())).thenReturn(Optional.of(articleData));

        when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles")
            .then()
            .statusCode(422);

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
}