package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryService  {
    private UserReadService userReadService;
    private JwtService jwtService;

    public UserQueryService(UserReadService userReadService, JwtService jwtService) {
        this.userReadService = userReadService;
        this.jwtService = jwtService;
    }

    public Optional<UserData> findById(String id) {
        return Optional.ofNullable(userReadService.findById(id));
    }
}

