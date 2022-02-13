package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.EncryptService;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UsersApi {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final EncryptService encryptService;
    private final JwtService jwtService;
    private final UserService userService;


    @PostMapping(path = "/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterParam registerParam) {
        var user = userService.createUser(registerParam);
        var userData = userQueryService.findById(user.getId()).get();
        return ResponseEntity
                .status(201)
                .body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
    }


    @PostMapping(path = "/users/login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        var optionalUser = userRepository.findByEmail(loginRequestDto.getEmail());
        if (optionalUser.isPresent()
                && encryptService.check(loginRequestDto.getPassword(), optionalUser.get().getPassword())) {
            var userData = userQueryService.findById(optionalUser.get().getId()).get();
            return ResponseEntity.ok(
                    userResponse(new UserWithToken(userData, jwtService.toToken(optionalUser.get()))));
        } else {
            throw new InvalidAuthenticationException();
        }
    }


    private Map<String, UserWithToken> userResponse(UserWithToken userWithToken) {
        return Map.of("user", userWithToken);
    }

}


@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginRequestDto {

    @NotBlank(message = "can't be empty")
    @Email(message = "should be an email")
    private String email;

    @NotBlank(message = "can't be empty")
    private String password;

}
