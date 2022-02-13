package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest({ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleApiTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArticleQueryService articleQueryService;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleCommandService articleCommandService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
    }


    @Test
    public void should_read_article_success() {
        var slug = "test-new-article";
        var time = new DateTime();
        var article = new Article(
                "Test New Article",
                "Desc",
                "Body",
                Arrays.asList("java", "spring", "jpg"),
                user.getId(),
                time
        );
        var articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

        when(articleQueryService.findBySlug(eq(slug), eq(null))).thenReturn(Optional.of(articleData));

        RestAssuredMockMvc.when()
                .get("/articles/{slug}", slug)
                .then()
                .statusCode(200)
                .body("article.slug", equalTo(slug))
                .body("article.body", equalTo(articleData.getBody()))
                .body("article.createdAt", equalTo(ISODateTimeFormat.dateTime().withZoneUTC().print(time)));
    }


    @Test
    public void should_404_if_article_not_found() {
        when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.empty());
        RestAssuredMockMvc.when().get("/articles/not-exists").then().statusCode(404);
    }


    @Test
    public void should_update_article_content_success() {
        var tagList = Arrays.asList("java", "spring", "jpg");
        var originalArticle = new Article("old title", "old description", "old body", tagList, user.getId());
        var updatedArticle = new Article("new title", "new description", "new body", tagList, user.getId());
        var updateParam = prepareUpdateParam(
                updatedArticle.getTitle(),
                updatedArticle.getBody(),
                updatedArticle.getDescription()
        );
        var updatedArticleData = TestHelper.getArticleDataFromArticleAndUser(updatedArticle, user);

        when(articleRepository.findBySlug(eq(originalArticle.getSlug()))).thenReturn(Optional.of(originalArticle));
        when(articleCommandService.updateArticle(eq(originalArticle), any())).thenReturn(updatedArticle);
        when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), eq(user)))
                .thenReturn(Optional.of(updatedArticleData));

        given()
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .body(updateParam)
                .when()
                .put("/articles/{slug}", originalArticle.getSlug())
                .then()
                .statusCode(200)
                .body("article.slug", equalTo(updatedArticleData.getSlug()));
    }

    @Test
    public void should_get_403_if_not_author_to_update_article() {
        var title = "new-title";
        var body = "new body";
        var description = "new description";
        var updateParam = prepareUpdateParam(title, body, description);
        var anotherUser = new User("test@test.com", "test", "123123", "", "");
        var article = new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), anotherUser.getId());
        var time = new DateTime();
        var profileData = new ProfileData(
                anotherUser.getId(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage(),
                false
        );
        var articleData = new ArticleData(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getDescription(),
                article.getBody(),
                false,
                0,
                time,
                time,
                List.of("joda"),
                profileData
        );

        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
        when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
                .thenReturn(Optional.of(articleData));

        given()
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .body(updateParam)
                .when()
                .put("/articles/{slug}", article.getSlug())
                .then()
                .statusCode(403);
    }


    @Test
    public void should_delete_article_success() {
        var title = "title";
        var body = "body";
        var description = "description";
        var article = new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), user.getId());

        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/{slug}", article.getSlug())
                .then()
                .statusCode(204);

        verify(articleRepository).remove(eq(article));
    }

    @Test
    public void should_403_if_not_author_delete_article() {
        var title = "new-title";
        var body = "new body";
        var description = "new description";
        var anotherUser = new User("test@test.com", "test", "123123", "", "");
        var article = new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), anotherUser.getId());

        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

        given()
                .header("Authorization", "Token " + token)
                .when()
                .delete("/articles/{slug}", article.getSlug())
                .then()
                .statusCode(403);
    }


    private Map<String, Map<String, String>> prepareUpdateParam(
            final String title,
            final String body,
            final String description
    ) {
        return Map.of("article", Map.of("title", title,
                "body", body,
                "description", description)
        );
    }

}
