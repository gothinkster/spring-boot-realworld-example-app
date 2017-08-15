package io.spring.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.JwtService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService  {
    private UserReadService userReadService;
    private JwtService jwtService;

    public UserQueryService(UserReadService userReadService, JwtService jwtService) {
        this.userReadService = userReadService;
        this.jwtService = jwtService;
    }

    public UserWithToken fetchNewAuthenticatedUser(String username) {
        UserData userData = userReadService.findByUsername(username);
        return new UserWithToken(userData, jwtService.toToken(userData));
    }

    public UserWithToken fetchCurrentUser(String username, String token) {
        return new UserWithToken(userReadService.findByUsername(username), token);
    }
}

