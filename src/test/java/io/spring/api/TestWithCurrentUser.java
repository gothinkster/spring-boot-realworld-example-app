package io.spring.api;

import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
abstract class TestWithCurrentUser {
    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected UserReadService userReadService;

    protected User user;
    protected UserData userData;
    protected String token;
    protected String email;
    protected String username;
    protected String defaultAvatar;

    @MockBean
    protected JwtService jwtService;

    protected void userFixture() {
        email = "john@jacob.com";
        username = "johnjacob";
        defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

        user = new User(email, username, "123", "", defaultAvatar);
        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(user));
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        userData = new UserData(user.getId(), email, username, "", defaultAvatar);
        when(userReadService.findById(eq(user.getId()))).thenReturn(userData);

        token = "token";
        when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
    }

    @Before
    public void setUp() throws Exception {
        userFixture();
    }
}
