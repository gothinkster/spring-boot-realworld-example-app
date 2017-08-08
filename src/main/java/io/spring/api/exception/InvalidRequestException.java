package io.spring.api.exception;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

@SuppressWarnings("serial")
@JsonRootName("errors")
public class InvalidRequestException extends RuntimeException {
    private final Errors errors;

    public InvalidRequestException(Errors errors) {
        super("");
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }
}
