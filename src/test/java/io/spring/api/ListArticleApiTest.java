package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.Page;
import io.spring.application.article.ArticleDataList;
import io.spring.application.article.ArticleQueryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ListArticleApiTest extends TestWithCurrentUser {
    @MockBean
    private ArticleQueryService articleQueryService;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        userFixture();
    }

    @Test
    public void should_get_default_article_list() throws Exception {
        ArticleDataList articleDataList = new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
        when(articleQueryService.findRecentArticles(eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(null))).thenReturn(articleDataList);
        RestAssured.when()
            .get("/articles")
            .prettyPeek()
            .then()
            .statusCode(200);
    }

    @Test
    public void should_get_feeds_401_without_login() throws Exception {
        RestAssured.when()
            .get("/articles/feed")
            .prettyPeek()
            .then()
            .statusCode(401);
    }

    @Test
    public void should_get_feeds_success() throws Exception {
        ArticleDataList articleDataList = new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
        when(articleQueryService.findUserFeed(eq(user), eq(new Page(0, 20)))).thenReturn(articleDataList);

        given()
            .header("Authorization", "Token " + token)
            .when()
            .get("/articles/feed")
            .prettyPeek()
            .then()
            .statusCode(200);
    }
}
