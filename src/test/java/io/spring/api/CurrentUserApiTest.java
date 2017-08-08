package io.spring.api;

import io.restassured.RestAssured;
import io.spring.application.JwtService;
import io.spring.application.user.UserData;
import io.spring.application.user.UserReadService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.service.DefaultJwtService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CurrentUserApiTest {
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserReadService userReadService;

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @Test
    public void should_get_current_user_with_token() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob";

        User user = new User(email, username, "123", "", "https://static.productionready.io/images/smiley-cyrus.jpg");
        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(user));

        UserData userData = new UserData(email, username, "", "https://static.productionready.io/images/smiley-cyrus.jpg");
        when(userReadService.findOne(eq(username))).thenReturn(userData);

        String token = jwtService.toToken(userData);

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
}
