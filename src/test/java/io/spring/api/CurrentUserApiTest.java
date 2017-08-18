package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.JwtService;
import io.spring.application.user.UserData;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CurrentUserApiTest extends TestWithCurrentUser {

    @LocalServerPort
    private int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
        userFixture();
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
            .body("user.image", equalTo(defaultAvatar))
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
        String newUsername = "newusernamee";

        Map<String, Object> param = new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", newEmail);
                put("bio", newBio);
                put("username", newUsername);
            }});
        }};

        when(userReadService.findByUsername(eq(newUsername))).thenReturn(new UserData(user.getId(), newEmail, newUsername, newBio, user.getImage()));

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .put("/user")
            .then()
            .statusCode(200)
            .body("user.token", not(token));
    }

    @Test
    public void should_get_error_if_email_exists_when_update_user_profile() throws Exception {
        String newEmail = "newemail@example.com";
        String newBio = "updated";
        String newUsername = "newusernamee";

        Map<String, Object> param = prepareUpdateParam(newEmail, newBio, newUsername);

        when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.of(new User(newEmail, "username", "123", "", "")));
        when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());

        when(userReadService.findByUsername(eq(newUsername))).thenReturn(new UserData(user.getId(), newEmail, newUsername, newBio, user.getImage()));

        given()
            .contentType("application/json")
            .header("Authorization", "Token " + token)
            .body(param)
            .when()
            .put("/user")
            .prettyPeek()
            .then()
            .statusCode(422)
            .body("errors.email[0]", equalTo("email already exist"));

    }

    private HashMap<String, Object> prepareUpdateParam(final String newEmail, final String newBio, final String newUsername) {
        return new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", newEmail);
                put("bio", newBio);
                put("username", newUsername);
            }});
        }};
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
