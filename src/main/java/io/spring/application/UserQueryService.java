package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserReadService userReadService;


    public Optional<UserData> findById(String id) {
        return Optional.ofNullable(userReadService.findById(id));
    }

}

