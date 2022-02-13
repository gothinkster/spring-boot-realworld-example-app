package io.spring.application.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = DuplicatedUsernameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface DuplicatedUsernameConstraint {

    String message() default "duplicated username";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
