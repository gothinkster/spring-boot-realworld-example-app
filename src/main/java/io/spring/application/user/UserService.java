package io.spring.application.user;

import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final String defaultImage;
    private final EncryptService encryptService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            @Value("${image.default}") String defaultImage,
            EncryptService encryptService
    ) {
        this.userRepository = userRepository;
        this.defaultImage = defaultImage;
        this.encryptService = encryptService;
    }

    public User createUser(@Valid RegisterParam registerParam) {
        var user = new User(
                registerParam.getEmail(),
                registerParam.getUsername(),
                encryptService.encrypt(registerParam.getPassword()),
                "",
                defaultImage
        );
        userRepository.save(user);
        return user;
    }

    public void updateUser(@Valid UpdateUserCommand command) {
        var user = command.getTargetUser();
        var updateUserParam = command.getParam();
        user.update(
                updateUserParam.getEmail(),
                updateUserParam.getUsername(),
                updateUserParam.getPassword(),
                updateUserParam.getBio(),
                updateUserParam.getImage()
        );
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
        var inputEmail = value.getParam().getEmail();
        var inputUsername = value.getParam().getUsername();
        var targetUser = value.getTargetUser();

        boolean isEmailValid = userRepository
                .findByEmail(inputEmail)
                .map(user -> user.equals(targetUser))
                .orElse(true);
        boolean isUsernameValid = userRepository
                .findByUsername(inputUsername)
                .map(user -> user.equals(targetUser))
                .orElse(true);
        if (isEmailValid && isUsernameValid) {
            return true;
        }

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
