package io.spring.infrastructure.user;

import io.spring.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserMapper {
    void insert(@Param("user") User user);

    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);

    User findById(@Param("id") String id);

    void update(@Param("user") User user);
}
