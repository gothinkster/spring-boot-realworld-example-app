package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.JwtService;
import io.spring.application.user.UserData;
import io.spring.application.user.UserReadService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CurrentUserApiTest extends TestWithCurrentUser {

    @LocalServerPort
    private int port;

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
    public void should_get_current_user_with_token() throws Exception {


        given()
            .header("Authorization", "Token " + token)
            .contentType("application/json")
            .when()
            .get("/user")
            .then()
            .statusCode(200)
            .body("user.email", equalTo(email))
            .body("user.username", equalTo(username))
            .body("user.bio", equalTo(""))
            .body("user.image", equalTo("https://static.productionready.io/images/smiley-cyrus.jpg"))
            .body("user.token", equalTo(token));
    }

    @Test
    public void should_get_401_without_token() throws Exception {
        given()
            .contentType("application/json")
            .when()
            .get("/user")
            .then()
            .statusCode(401);

    }

    @Test
    public void should_get_401_with_invalid_token() throws Exception {
        given()
            .contentType("application/json")
            .header("Authorization", "Token asdfasd")
            .when()
            .get("/user")
            .then()
            .statusCode(401);
    }

    @Test
    public void should_update_current_user_profile() throws Exception {
        String newEmail = "newemail@example.com";
        String newBio = "updated";

        Map<String, Object> param = new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", newEmail);
                put("bio", newBio);
            }});
        }};

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .put("/user")
            .then()
            .statusCode(200);

        assertThat(user.getEmail(), is(newEmail));
        assertThat(user.getBio(), is(newBio));
        assertThat(user.getImage(), is(defaultAvatar));
    }

    @Test
    public void should_get_401_if_not_login() throws Exception {
        given()
            .contentType("application/json")
            .body(new HashMap<String, Object>() {{
                put("user", new HashMap<String, Object>());
            }})
            .when()
            .put("/user")
            .then().statusCode(401);
    }
}
