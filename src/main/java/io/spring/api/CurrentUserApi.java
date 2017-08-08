package io.spring.api;

import io.spring.application.user.UserQueryService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserApi {
    private UserQueryService userQueryService;

    @Autowired
    public CurrentUserApi(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public ResponseEntity currentUser(@AuthenticationPrincipal User currentUser,
                                      @RequestHeader(value = "Authorization") String authorization) {
        return ResponseEntity.ok(userQueryService.fetchCurrentUser(currentUser.getUsername(), authorization.split(" ")[1]));
    }

}
