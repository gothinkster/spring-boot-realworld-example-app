package io.spring.application;

import io.spring.application.user.UserData;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface JwtService {
    String toToken(UserData userData);

    Optional<String> getSubFromToken(String token);
}
