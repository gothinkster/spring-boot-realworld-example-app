package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArticleQueryService articleQueryService;

    @MockBean
    private ArticleCommandService articleCommandService;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
    }


    @Test
    public void should_create_article_success() {
        var title = "How to train your dragon";
        var slug = "how-to-train-your-dragon";
        var description = "Ever wonder how?";
        var body = "You have to believe";
        var tagList = asList("reactjs", "angularjs", "dragons");
        var param = prepareParam(title, description, body, tagList);
        var profileData = new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false);
        var articleData = new ArticleData(
                "123",
                slug,
                title,
                description,
                body,
                false,
                0,
                new DateTime(),
                new DateTime(),
                tagList,
                profileData
        );

        when(articleCommandService.createArticle(any(), any()))
                .thenReturn(new Article(title, description, body, tagList, user.getId()));

        when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
                .thenReturn(Optional.empty());

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

        verify(articleCommandService).createArticle(any(), any());
    }


    @Test
    public void should_get_error_message_with_wrong_parameter() {
        var title = "How to train your dragon";
        var description = "Ever wonder how?";
        var body = "";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        var param = prepareParam(title, description, body, asList(tagList));

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
        var title = "How to train your dragon";
        var slug = "how-to-train-your-dragon";
        var description = "Ever wonder how?";
        var body = "You have to believe";
        String[] tagList = {"reactjs", "angularjs", "dragons"};
        var param = prepareParam(title, description, body, asList(tagList));
        var profileData = new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false);
        var articleData = new ArticleData(
                "123",
                slug,
                title,
                description,
                body,
                false,
                0,
                new DateTime(),
                new DateTime(),
                asList(tagList),
                profileData
        );

        when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
                .thenReturn(Optional.of(articleData));

        when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

        given()
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .body(param)
                .when()
                .post("/articles")
                .prettyPeek()
                .then()
                .statusCode(422);
    }


    private Map<String, Map<String, Object>> prepareParam(
            String title,
            String description,
            String body,
            List<String> tagList
    ) {
        return Map.of("article", Map.of("title", title,
                "description", description,
                "body", body,
                "tagList", tagList)
        );
    }

}
