package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidRequestException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserWithToken;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        UserData userData = userQueryService.findById(currentUser.getId()).get();
        return ResponseEntity.ok(userResponse(
            new UserWithToken(userData, authorization.split(" ")[1])
        ));
    }

    @PutMapping
    public ResponseEntity updateProfile(@AuthenticationPrincipal User currentUser,
                                        @RequestHeader("Authorization") String token,
                                        @Valid @RequestBody UpdateUserParam updateUserParam,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
        checkUniquenessOfUsernameAndEmail(currentUser, updateUserParam, bindingResult);

        currentUser.update(
            updateUserParam.getEmail(),
            updateUserParam.getUsername(),
            updateUserParam.getPassword(),
            updateUserParam.getBio(),
            updateUserParam.getImage());
        userRepository.save(currentUser);
        UserData userData = userQueryService.findById(currentUser.getId()).get();
        return ResponseEntity.ok(userResponse(
            new UserWithToken(userData, token.split(" ")[1])
        ));
    }

    private void checkUniquenessOfUsernameAndEmail(User currentUser, UpdateUserParam updateUserParam, BindingResult bindingResult) {
        if (!"".equals(updateUserParam.getUsername())) {
            Optional<User> byUsername = userRepository.findByUsername(updateUserParam.getUsername());
            if (byUsername.isPresent() && !byUsername.get().equals(currentUser)) {
                bindingResult.rejectValue("username", "DUPLICATED", "username already exist");
            }
        }

        if (!"".equals(updateUserParam.getEmail())) {
            Optional<User> byEmail = userRepository.findByEmail(updateUserParam.getEmail());
            if (byEmail.isPresent() && !byEmail.get().equals(currentUser)) {
                bindingResult.rejectValue("email", "DUPLICATED", "email already exist");
            }
        }

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
    }

    private Map<String, Object> userResponse(UserWithToken userWithToken) {
        return new HashMap<String, Object>() {{
            put("user", userWithToken);
        }};
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
