package io.spring.application;

import io.spring.application.user.UserData;

public interface JwtService {
    String toToken(UserData userData);
}
