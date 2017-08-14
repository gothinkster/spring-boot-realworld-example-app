package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.user.UserQueryService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/user")
public class CurrentUserApi {
    private UserQueryService userQueryService;
    private UserRepository userRepository;

    @Autowired
    public CurrentUserApi(UserQueryService userQueryService, UserRepository userRepository) {
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity currentUser(@AuthenticationPrincipal User currentUser,
                                      @RequestHeader(value = "Authorization") String authorization) {
        return ResponseEntity.ok(userQueryService.fetchCurrentUser(currentUser.getUsername(), authorization.split(" ")[1]));
    }

    @PutMapping
    public ResponseEntity updateProfile(@AuthenticationPrincipal User currentUser,
                                        @RequestHeader(value = "Authorization") String authorization,
                                        @Valid @RequestBody UpdateUserParam updateUserParam,
                                        BindingResult bindingResult) {
        currentUser.update(
            updateUserParam.getEmail(),
            updateUserParam.getUsername(),
            updateUserParam.getPassword(),
            updateUserParam.getBio(),
            updateUserParam.getImage());
        userRepository.save(currentUser);
        return ResponseEntity.ok(userQueryService.fetchCurrentUser(currentUser.getUsername(), authorization.split(" ")[1]));
    }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class UpdateUserParam {
    @Email(message = "should be an email")
    private String email = "";
    private String password = "";
    private String username = "";
    private String bio = "";
    private String image = "";
}
