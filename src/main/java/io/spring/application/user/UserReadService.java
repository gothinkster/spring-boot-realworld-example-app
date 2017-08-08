package io.spring.application.user;

import org.springframework.data.repository.CrudRepository;

public interface UserReadService extends CrudRepository<UserData, String> {

}

