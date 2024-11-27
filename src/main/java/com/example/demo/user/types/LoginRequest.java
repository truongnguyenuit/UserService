package com.example.demo.user.types;

public record LoginRequest(
        String email,
        String password
) {
}
