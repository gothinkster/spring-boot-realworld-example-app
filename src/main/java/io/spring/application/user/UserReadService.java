package io.spring.application.user;

import io.spring.application.profile.ProfileData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserReadService {

    UserData findByUsername(@Param("username") String username);
}

