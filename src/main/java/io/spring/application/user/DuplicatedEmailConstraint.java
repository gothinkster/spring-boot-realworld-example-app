package io.spring.application.user;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = DuplicatedEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicatedEmailConstraint {

    String message() default "duplicated email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
