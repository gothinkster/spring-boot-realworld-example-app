package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.ArticleRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ArticlesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ListArticleApiTest extends TestWithCurrentUser {
    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleQueryService articleQueryService;

    @Autowired
    private MockMvc mvc;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    public void should_get_default_article_list() throws Exception {
        ArticleDataList articleDataList = new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
        when(articleQueryService.findRecentArticles(eq(null), eq(null), eq(null), eq(new Page(0, 20)), eq(null))).thenReturn(articleDataList);
        RestAssuredMockMvc.when()
            .get("/articles")
            .prettyPeek()
            .then()
            .statusCode(200);
    }

    @Test
    public void should_get_feeds_401_without_login() throws Exception {
        RestAssuredMockMvc.when()
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
