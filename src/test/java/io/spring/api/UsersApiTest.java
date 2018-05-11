package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.NaiveEncryptService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(UsersApi.class)
@Import({WebSecurityConfig.class, UserQueryService.class, NaiveEncryptService.class, JacksonCustomizations.class})
public class UsersApiTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserReadService userReadService;
    private String defaultAvatar;


    @Before
    public void setUp() throws Exception {
        RestAssuredMockMvc.mockMvc(mvc);
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
    }

    @Test
    public void should_create_user_success() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob";

        when(jwtService.toToken(any())).thenReturn("123");
        User user = new User(email, username, "123", "", defaultAvatar);
        UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);
        when(userReadService.findById(any())).thenReturn(userData);

        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());
        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        Map<String, Object> param = prepareRegisterParameter(email, username);

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .body("user.email", equalTo(email))
            .body("user.username", equalTo(username))
            .body("user.bio", equalTo(""))
            .body("user.image", equalTo(defaultAvatar))
            .body("user.token", equalTo("123"));

        verify(userRepository).save(any());
    }

    @Test
    public void should_show_error_message_for_blank_username() throws Exception {

        String email = "john@jacob.com";
        String username = "";

        Map<String, Object> param = prepareRegisterParameter(email, username);

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users")
            .then()
            .statusCode(422)
            .body("errors.username[0]", equalTo("can't be empty"));
    }

    @Test
    public void should_show_error_message_for_invalid_email() throws Exception {
        String email = "johnxjacob.com";
        String username = "johnjacob";

        Map<String, Object> param = prepareRegisterParameter(email, username);

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users")
            .then()
            .statusCode(422)
            .body("errors.email[0]", equalTo("should be an email"));

    }

    @Test
    public void should_show_error_for_duplicated_username() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob";

        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(new User(
            email, username, "123", "bio", ""
        )));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Map<String, Object> param = prepareRegisterParameter(email, username);

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users")
            .then()
            .statusCode(422)
            .body("errors.username[0]", equalTo("duplicated username"));
    }

    @Test
    public void should_show_error_for_duplicated_email() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob2";

        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(new User(
            email, username, "123", "bio", ""
        )));

        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());

        Map<String, Object> param = prepareRegisterParameter(email, username);

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users")
            .then()
            .statusCode(422)
            .body("errors.email[0]", equalTo("duplicated email"));
    }

    private HashMap<String, Object> prepareRegisterParameter(final String email, final String username) {
        return new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", email);
                put("password", "johnnyjacob");
                put("username", username);
            }});
        }};
    }

    @Test
    public void should_login_success() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob2";
        String password = "123";

        User user = new User(email, username, password, "", defaultAvatar);
        UserData userData = new UserData("123", email, username, "", defaultAvatar);

        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
        when(userReadService.findByUsername(eq(username))).thenReturn(userData);
        when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
        when(jwtService.toToken(any())).thenReturn("123");

        Map<String, Object> param = new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", email);
                put("password", password);
            }});
        }};

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users/login")
            .then()
            .statusCode(200)
            .body("user.email", equalTo(email))
            .body("user.username", equalTo(username))
            .body("user.bio", equalTo(""))
            .body("user.image", equalTo(defaultAvatar))
            .body("user.token", equalTo("123"));;
    }

    @Test
    public void should_fail_login_with_wrong_password() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob2";
        String password = "123";

        User user = new User(email, username, password, "", defaultAvatar);
        UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);

        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
        when(userReadService.findByUsername(eq(username))).thenReturn(userData);

        Map<String, Object> param = new HashMap<String, Object>() {{
            put("user", new HashMap<String, Object>() {{
                put("email", email);
                put("password", "123123");
            }});
        }};

        given()
            .contentType("application/json")
            .body(param)
            .when()
            .post("/users/login")
            .then()
            .statusCode(422)
            .body("errors.password[0]", equalTo("invalid email or password"));
    }
}
