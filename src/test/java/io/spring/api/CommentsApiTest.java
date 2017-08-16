package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.comment.CommentData;
import io.spring.application.comment.CommentQueryService;
import io.spring.application.profile.ProfileData;
import io.spring.application.user.UserData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
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
    private CommentData commentData;
    private Comment comment;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        email = "john@jacob.com";
        username = "johnjacob";
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
        userFixture();

        article = new Article("title", "desc", "body", new String[]{"test", "java"}, user.getId());
        when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
        comment = new Comment("comment", user.getId(), article.getId());
        commentData = new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    }

    @Test
    public void should_create_comment_success() throws Exception {
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("comment", new HashMap<String, Object>() {{
                put("body", "comment content");
            }});
        }};

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

    @Test
    public void should_get_comments_of_article_success() throws Exception {
        when(commentQueryService.findByArticleSlug(anyString(), eq(null))).thenReturn(Arrays.asList(commentData));
        RestAssured.when()
            .get("/articles/{slug}/comments", article.getSlug())
            .prettyPeek()
            .then()
            .statusCode(200)
            .body("comments[0].id", equalTo(commentData.getId()));
    }

    @Test
    public void should_delete_comment_success() throws Exception {
        when(commentRepository.findById(eq(article.getId()), eq(comment.getId()))).thenReturn(Optional.of(comment));

        given()
            .header("Authorization", "Token " + token)
            .when()
            .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
            .then()
            .statusCode(204);
    }

    @Test
    public void should_get_403_if_not_author_of_article_or_author_of_comment_when_delete_comment() throws Exception {
        User anotherUser = new User("other@example.com", "other", "123", "", "");
        when(userRepository.findByUsername(eq(anotherUser.getUsername()))).thenReturn(Optional.of(anotherUser));

        when(commentRepository.findById(eq(article.getId()), eq(comment.getId()))).thenReturn(Optional.of(comment));
        String token = jwtService.toToken(
            new UserData(
                anotherUser.getId(),
                anotherUser.getEmail(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage()));
        given()
            .header("Authorization", "Token " + token)
            .when()
            .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
            .then()
            .statusCode(403);

    }
}
