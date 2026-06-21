package io.spring.application.user;

import io.spring.api.LoginParam;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final String defaultImage;
  private final PasswordEncoder passwordEncoder;
  private final UserQueryService userQueryService;
  private final JwtService jwtService;



    @Autowired
  public UserService(
      UserRepository userRepository,
      UserQueryService userQueryService,
      JwtService jwtService,
      @Value("${image.default}") String defaultImage,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
        this.userQueryService = userQueryService;
        this.jwtService = jwtService;
        this.defaultImage = defaultImage;
    this.passwordEncoder = passwordEncoder;
  }

    public UserWithToken createUser(@Valid RegisterParam registerParam) {

        log.info("Creating user with email={}", registerParam.getEmail());

        User user = new User(
                registerParam.getEmail(),
                registerParam.getUsername(),
                passwordEncoder.encode(registerParam.getPassword()),
                "",
                defaultImage
        );

        User savedUser = userRepository.save(user);

        log.info("User created successfully with id={}", savedUser.getId());

        log.info("savedUser = {}", savedUser);
        log.info("savedUser.id = {}", savedUser.getId());

        UserData userData = userQueryService.findById(savedUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + savedUser.getId()));

        log.debug("UserData loaded for userId={}, email={}",
                savedUser.getId(), userData.getEmail());

        String token = jwtService.toToken(savedUser);

        log.debug("JWT generated for userId={}", savedUser.getId());

        return new UserWithToken(userData, token);
    }

  public UserWithToken login(@Valid LoginParam loginParam)
  {
      User user = userRepository.findByEmail(loginParam.getEmail())
              .orElseThrow(InvalidAuthenticationException::new);

      if (!passwordEncoder.matches(
              loginParam.getPassword(),
              user.getPassword())) {
          throw new InvalidAuthenticationException();
      };

      UserData userData = userQueryService.findById(user.getId())
              .orElseThrow();

      return new UserWithToken(
              userData,
              jwtService.toToken(user)
      );
  }

  public void updateUser(@Valid UpdateUserCommand command) {
    User user = command.getTargetUser();
    UpdateUserParam updateUserParam = command.getParam();
    user.update(
        updateUserParam.getEmail(),
        updateUserParam.getUsername(),
        updateUserParam.getPassword(),
        updateUserParam.getBio(),
        updateUserParam.getImage());
    userRepository.save(user);
  }
}

@Constraint(validatedBy = UpdateUserValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface UpdateUserConstraint {

  String message() default "invalid update param";

  Class[] groups() default {};

  Class[] payload() default {};
}

class UpdateUserValidator implements ConstraintValidator<UpdateUserConstraint, UpdateUserCommand> {

  @Autowired
  private UserRepository userRepository;

  @Override
  public boolean isValid(UpdateUserCommand value, ConstraintValidatorContext context) {
    String inputEmail = value.getParam().getEmail();
    String inputUsername = value.getParam().getUsername();
    final User targetUser = value.getTargetUser();

    boolean isEmailValid =
        userRepository.findByEmail(inputEmail).map(user -> user.equals(targetUser)).orElse(true);
    boolean isUsernameValid =
        userRepository
            .findByUsername(inputUsername)
            .map(user -> user.equals(targetUser))
            .orElse(true);
    if (isEmailValid && isUsernameValid) {
      return true;
    } else {
      context.disableDefaultConstraintViolation();
      if (!isEmailValid) {
        context
            .buildConstraintViolationWithTemplate("email already exist")
            .addPropertyNode("email")
            .addConstraintViolation();
      }
      if (!isUsernameValid) {
        context
            .buildConstraintViolationWithTemplate("username already exist")
            .addPropertyNode("username")
            .addConstraintViolation();
      }
      return false;
    }
  }
}
