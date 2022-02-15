package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {
  private UserReadService userReadService;

  public UserQueryService(UserReadService userReadService) {
    this.userReadService = userReadService;
  }

  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
