package io.spring.api.exception;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<Object> handleInvalidRequest(
            RuntimeException exception,
            WebRequest request
    ) {
        var ire = (InvalidRequestException) exception;
        var errorResources = ire.getErrors()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()
                ))
                .toList();
        var error = new ErrorResource(errorResources);
        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return handleExceptionInternal(exception, error, headers, UNPROCESSABLE_ENTITY, request);
    }


    @ExceptionHandler(InvalidAuthenticationException.class)
    public ResponseEntity<Object> handleInvalidAuthentication(
            InvalidAuthenticationException exception,
            WebRequest request
    ) {
        return ResponseEntity
                .status(UNPROCESSABLE_ENTITY)
                .body(Map.of("message", exception.getMessage()));
    }


    @NotNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatus status,
            @NotNull WebRequest request
    ) {
        var errorResources = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .status(UNPROCESSABLE_ENTITY)
                .body(new ErrorResource(errorResources));
    }


    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResource handleConstraintViolation(
            ConstraintViolationException exception,
            WebRequest request
    ) {
        var errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorResource(
                        violation.getRootBeanClass().getName(),
                        getParam(violation.getPropertyPath().toString()),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        violation.getMessage()
                ))
                .toList();

        return new ErrorResource(errors);
    }


    private String getParam(String str) {
        var splits = str.split("\\.");
        return splits.length == 1 ?
                str :
                String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }

}
