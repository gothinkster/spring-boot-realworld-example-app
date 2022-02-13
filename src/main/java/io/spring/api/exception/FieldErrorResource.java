package io.spring.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@RequiredArgsConstructor
public class FieldErrorResource {

    private final String resource;
    private final String field;
    private final String code;
    private final String message;

}
