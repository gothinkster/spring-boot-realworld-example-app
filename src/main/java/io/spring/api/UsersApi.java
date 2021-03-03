package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.core.service.JwtService;
import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersApi {
  private UserRepository userRepository;
  private UserQueryService userQueryService;
  private String defaultImage;
  private EncryptService encryptService;
  private JwtService jwtService;

  @Autowired
  public UsersApi(
      UserRepository userRepository,
      UserQueryService userQueryService,
      EncryptService encryptService,
      @Value("${image.default}") String defaultImage,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.userQueryService = userQueryService;
    this.encryptService = encryptService;
    this.defaultImage = defaultImage;
    this.jwtService = jwtService;
  }

  @RequestMapping(path = "/users", method = POST)
  public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam) {
    User user =
        new User(
            registerParam.getEmail(),
            registerParam.getUsername(),
            encryptService.encrypt(registerParam.getPassword()),
            "",
            defaultImage);
    userRepository.save(user);
    UserData userData = userQueryService.findById(user.getId()).get();
    return ResponseEntity.status(201)
        .body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
  }

  @RequestMapping(path = "/users/login", method = POST)
  public ResponseEntity userLogin(@Valid @RequestBody LoginParam loginParam) {
    Optional<User> optional = userRepository.findByEmail(loginParam.getEmail());
    if (optional.isPresent()
        && encryptService.check(loginParam.getPassword(), optional.get().getPassword())) {
      UserData userData = userQueryService.findById(optional.get().getId()).get();
      return ResponseEntity.ok(
          userResponse(new UserWithToken(userData, jwtService.toToken(optional.get()))));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}

@Constraint(validatedBy = DuplicatedEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface DuplicatedEmailConstraint {
  String message() default "duplicated email";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

class DuplicatedEmailValidator implements ConstraintValidator<DuplicatedEmailConstraint, String> {

  @Autowired private UserRepository userRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return (value == null || value.isEmpty()) || !userRepository.findByEmail(value).isPresent();
  }
}

@Constraint(validatedBy = DuplicatedUsernameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface DuplicatedUsernameConstraint {
  String message() default "duplicated username";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

class DuplicatedUsernameValidator
    implements ConstraintValidator<DuplicatedUsernameConstraint, String> {

  @Autowired private UserRepository userRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return (value == null || value.isEmpty()) || !userRepository.findByUsername(value).isPresent();
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class RegisterParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  @DuplicatedEmailConstraint
  private String email;

  @NotBlank(message = "can't be empty")
  @DuplicatedUsernameConstraint
  private String username;

  @NotBlank(message = "can't be empty")
  private String password;
}
