package io.spring.api.exception;

import org.springframework.validation.Errors;

@SuppressWarnings("serial")
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
