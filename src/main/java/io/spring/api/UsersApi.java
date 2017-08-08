package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidRequestException;
import io.spring.application.user.UserQueryService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersApi {
    private UserRepository userRepository;
    private UserQueryService userQueryService;
    private String defaultImage;

    @Autowired
    public UsersApi(UserRepository userRepository, UserQueryService userQueryService, @Value("${image.default}") String defaultImage) {
        this.userRepository = userRepository;
        this.userQueryService = userQueryService;
        this.defaultImage = defaultImage;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity creeteUser(@Valid @RequestBody RegisterParam registerParam, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
        if (userRepository.findByUsername(registerParam.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "DUPLICATED", "duplicated username");
            throw new InvalidRequestException(bindingResult);
        }

        if (userRepository.findByEmail(registerParam.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "DUPLICATED", "duplicated email");
            throw new InvalidRequestException(bindingResult);
        }
        User user = new User(
            registerParam.getEmail(),
            registerParam.getUsername(),
            registerParam.getPassword(),
            "",
            defaultImage);
        userRepository.save(user);
        return ResponseEntity.status(201).body(userQueryService.fetchCreatedUser(user.getUsername()));
    }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class RegisterParam {
    @NotBlank(message = "can't be empty")
    @Email(message = "should be an email")
    private String email;
    @NotBlank(message = "can't be empty")
    private String username;
    @NotBlank(message = "can't be empty")
    private String password;
}
