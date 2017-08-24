package io.spring.infrastructure.service;

import io.spring.core.user.EncryptService;
import org.springframework.stereotype.Service;

@Service
public class NaiveEncryptService implements EncryptService {
    @Override
    public String encrypt(String password) {
        return password;
    }

    @Override
    public boolean check(String checkPassword, String realPassword) {
        return checkPassword.equals(realPassword);
    }
}
