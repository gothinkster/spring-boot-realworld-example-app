package io.spring.graphql.exception;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import io.spring.api.exception.FieldErrorResource;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GraphQLCustomizeExceptionHandler implements DataFetcherExceptionHandler {

    private final DefaultDataFetcherExceptionHandler defaultHandler = new DefaultDataFetcherExceptionHandler();

    @Override
    public DataFetcherExceptionHandlerResult onException(
            DataFetcherExceptionHandlerParameters handlerParameters
    ) {
        var exception = handlerParameters.getException();
        if (exception instanceof InvalidAuthenticationException) {
            var graphqlError = TypedGraphQLError.newBuilder()
                    .errorType(ErrorType.UNAUTHENTICATED)
                    .message(exception.getMessage())
                    .path(handlerParameters.getPath())
                    .build();
            return DataFetcherExceptionHandlerResult.newResult()
                    .error(graphqlError)
                    .build();
        } else if (exception instanceof ConstraintViolationException) {
            var errors = new ArrayList<FieldErrorResource>();
            var constraintViolations = ((ConstraintViolationException) exception).getConstraintViolations();
            for (var violation : constraintViolations) {
                var fieldErrorResource = new FieldErrorResource(
                        violation.getRootBeanClass().getName(),
                        getParam(violation.getPropertyPath().toString()),
                        getSimpleName(violation),
                        violation.getMessage()
                );
                errors.add(fieldErrorResource);
            }
            var graphqlError = TypedGraphQLError.newBadRequestBuilder()
                    .message(exception.getMessage())
                    .path(handlerParameters.getPath())
                    .extensions(errorsToMap(errors))
                    .build();
            return DataFetcherExceptionHandlerResult.newResult()
                    .error(graphqlError)
                    .build();
        } else {
            return defaultHandler.onException(handlerParameters);
        }
    }

    @NotNull
    private static String getSimpleName(ConstraintViolation<?> violation) {
        return violation
                .getConstraintDescriptor()
                .getAnnotation()
                .annotationType()
                .getSimpleName();
    }

    public static Error getErrorsAsData(ConstraintViolationException cve) {
        var errors = new ArrayList<FieldErrorResource>();
        for (var violation : cve.getConstraintViolations()) {
            FieldErrorResource fieldErrorResource = new FieldErrorResource(
                    violation.getRootBeanClass().getName(),
                    getParam(violation.getPropertyPath().toString()),
                    getSimpleName(violation),

                    violation.getMessage());
            errors.add(fieldErrorResource);
        }
        var errorMap = new HashMap<String, List<String>>();
        for (var fieldErrorResource : errors) {
            if (!errorMap.containsKey(fieldErrorResource.getField())) {
                errorMap.put(fieldErrorResource.getField(), new ArrayList<>());
            }
            errorMap.get(fieldErrorResource.getField()).add(fieldErrorResource.getMessage());
        }
        var errorItems = errorMap.entrySet()
                .stream()
                .map(kv -> ErrorItem.newBuilder()
                        .key(kv.getKey())
                        .value(kv.getValue())
                        .build()
                )
                .toList();
        return Error.newBuilder()
                .message("BAD_REQUEST")
                .errors(errorItems)
                .build();
    }

    private static String getParam(String s) {
        String[] splits = s.split("\\.");
        if (splits.length == 1) {
            return s;
        } else {
            return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
        }
    }

    private static Map<String, Object> errorsToMap(List<FieldErrorResource> errors) {
        var json = new HashMap<String, Object>();
        for (var fieldErrorResource : errors) {
            if (!json.containsKey(fieldErrorResource.getField())) {
                json.put(fieldErrorResource.getField(), new ArrayList<>());
            }
            ((List) json.get(fieldErrorResource.getField())).add(fieldErrorResource.getMessage());
        }
        return json;
    }

}
