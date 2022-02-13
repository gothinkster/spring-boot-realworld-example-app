package io.spring.application.user;

import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static io.spring.Utils.isEmpty;

class DuplicatedUsernameValidator
        implements ConstraintValidator<DuplicatedUsernameConstraint, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (isEmpty(value)) || !userRepository.findByUsername(value).isPresent();
    }

}
