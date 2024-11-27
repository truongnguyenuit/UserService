package com.example.demo.user;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(Long userId);
}
