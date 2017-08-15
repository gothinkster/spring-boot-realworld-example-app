package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.comment.CommentData;
import io.spring.application.comment.CommentQueryService;
import io.spring.application.profile.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CommentsApiTest extends TestWithCurrentUser {
    @LocalServerPort
    private int port;

    protected String email;
    protected String username;
    protected String defaultAvatar;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private CommentQueryService commentQueryService;

    private Article article;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        email = "john@jacob.com";
        username = "johnjacob";
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
        userFixture(email, username, defaultAvatar);

        article = new Article("title", "desc", "body", new String[]{"test", "java"}, user.getId());
        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    }

    @Test
    public void should_create_comment_success() throws Exception {
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("comment", new HashMap<String, Object>() {{
                put("body", "comment content");
            }});
        }};

        CommentData commentData = new CommentData(
            "123",
            "comment",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

        when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles/{slug}/comments", article.getSlug())
            .then()
            .statusCode(201)
            .body("comment.body", equalTo(commentData.getBody()));
    }

    @Test
    public void should_get_422_with_empty_body() throws Exception {
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("comment", new HashMap<String, Object>() {{
                put("body", "");
            }});
        }};

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .post("/articles/{slug}/comments", article.getSlug())
            .then()
            .statusCode(422)
            .body("errors.body[0]", equalTo("can't be empty"));

    }
}
