package com.example.demo.user.types;


import com.example.demo.user.USER_ROLE;

public record AuthResponse(
        String jwt,
        String message,
        USER_ROLE role
) {
}
