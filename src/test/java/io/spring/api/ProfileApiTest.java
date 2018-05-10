package io.spring.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
    private User anotherUser;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProfileQueryService profileQueryService;

    private ProfileData profileData;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        RestAssuredMockMvc.mockMvc(mvc);
        anotherUser = new User("username@test.com", "username", "123", "", "");
        profileData = new ProfileData(anotherUser.getId(), anotherUser.getUsername(), anotherUser.getBio(), anotherUser.getImage(), false);
        when(userRepository.findByUsername(eq(anotherUser.getUsername()))).thenReturn(Optional.of(anotherUser));
    }

    @Test
    public void should_get_user_profile_success() throws Exception {
        when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(null)))
            .thenReturn(Optional.of(profileData));
        RestAssuredMockMvc.when()
            .get("/profiles/{username}", profileData.getUsername())
            .prettyPeek()
            .then()
            .statusCode(200)
            .body("profile.username", equalTo(profileData.getUsername()));
    }

    @Test
    public void should_follow_user_success() throws Exception {
        when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user))).thenReturn(Optional.of(profileData));
        given()
            .header("Authorization", "Token " + token)
            .when()
            .post("/profiles/{username}/follow", anotherUser.getUsername())
            .prettyPeek()
            .then()
            .statusCode(200);
        verify(userRepository).saveRelation(new FollowRelation(user.getId(), anotherUser.getId()));
    }

    @Test
    public void should_unfollow_user_success() throws Exception {
        FollowRelation followRelation = new FollowRelation(user.getId(), anotherUser.getId());
        when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId()))).thenReturn(Optional.of(followRelation));
        when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user))).thenReturn(Optional.of(profileData));

        given()
            .header("Authorization", "Token " + token)
            .when()
            .delete("/profiles/{username}/follow", anotherUser.getUsername())
            .prettyPeek()
            .then()
            .statusCode(200);

        verify(userRepository).removeRelation(eq(followRelation));
    }
}