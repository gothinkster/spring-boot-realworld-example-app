package io.spring.application;

import io.spring.application.user.UserData;

import java.util.Optional;

public interface JwtService {
    String toToken(UserData userData);

    Optional<String> getSubFromToken(String token);
}
