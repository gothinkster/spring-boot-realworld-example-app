package io.spring.api;

import io.spring.core.service.JwtService;
import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

class TestWithCurrentUser {
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

    @Autowired
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

        token = jwtService.toToken(user);
    }
}
