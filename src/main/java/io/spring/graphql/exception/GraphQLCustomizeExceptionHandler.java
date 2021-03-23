package io.spring.graphql.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import io.spring.api.exception.FieldErrorResource;
import io.spring.api.exception.InvalidAuthenticationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;

@Component
public class GraphQLCustomizeExceptionHandler implements DataFetcherExceptionHandler {

  private final DefaultDataFetcherExceptionHandler defaultHandler =
      new DefaultDataFetcherExceptionHandler();
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public DataFetcherExceptionHandlerResult onException(
      DataFetcherExceptionHandlerParameters handlerParameters) {
    if (handlerParameters.getException() instanceof InvalidAuthenticationException) {
      GraphQLError graphqlError =
          TypedGraphQLError.UNAUTHENTICATED
              .message(handlerParameters.getException().getMessage())
              .path(handlerParameters.getPath())
              .build();
      return DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build();
    } else if (handlerParameters.getException() instanceof ConstraintViolationException) {
      List<FieldErrorResource> errors = new ArrayList<>();
      for (ConstraintViolation<?> violation :
          ((ConstraintViolationException) handlerParameters.getException())
              .getConstraintViolations()) {
        FieldErrorResource fieldErrorResource =
            new FieldErrorResource(
                violation.getRootBeanClass().getName(),
                getParam(violation.getPropertyPath().toString()),
                violation
                    .getConstraintDescriptor()
                    .getAnnotation()
                    .annotationType()
                    .getSimpleName(),
                violation.getMessage());
        errors.add(fieldErrorResource);
      }
      GraphQLError graphqlError =
          TypedGraphQLError.BAD_REQUEST
              .message(handlerParameters.getException().getMessage())
              .path(handlerParameters.getPath())
              .debugInfo(errorsToMap(errors))
              .build();
      return DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build();
    } else {
      return defaultHandler.onException(handlerParameters);
    }
  }

  private String getParam(String s) {
    String[] splits = s.split("\\.");
    if (splits.length == 1) {
      return s;
    } else {
      return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }
  }

  private Map<String, Object> errorsToMap(List<FieldErrorResource> errors) {
    Map<String, Object> json = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : errors) {
      if (!json.containsKey(fieldErrorResource.getField())) {
        json.put(fieldErrorResource.getField(), new ArrayList<>());
      }
      ((List) json.get(fieldErrorResource.getField())).add(fieldErrorResource.getMessage());
    }
    return json;
  }
}
