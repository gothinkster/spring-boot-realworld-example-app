package io.spring.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonSerialize(using = ErrorResourceSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("errors")
@Getter
@RequiredArgsConstructor
public class ErrorResource {

    private final List<FieldErrorResource> fieldErrors;

}
