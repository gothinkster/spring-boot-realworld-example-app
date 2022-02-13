package io.spring.api;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
public class CurrentUserApi {

    private final UserQueryService userQueryService;
    private final UserService userService;


    @GetMapping
    public ResponseEntity<?> currentUser(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader(value = "Authorization") String authorization
    ) {
        var userData = findUserData(currentUser);
        return getResonse(userData, authorization);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateUserParam updateUserParam
    ) {
        userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
        var userData = findUserData(currentUser);
        return getResonse(userData, token);
    }


    @NotNull
    private ResponseEntity<Map<String, Object>> getResonse(UserData userData, String string) {
        return ResponseEntity.ok(userResponse(new UserWithToken(userData, string.split(" ")[1])));
    }


    @NotNull
    private UserData findUserData(User currentUser) {
        return userQueryService.findById(currentUser.getId()).get();
    }


    private Map<String, Object> userResponse(UserWithToken userWithToken) {
        return Map.of("user", userWithToken);
    }

}
