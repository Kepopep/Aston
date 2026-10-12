package com.example.DTO;

public record CreateUserRequest(
        String name,
        String email,
        Integer age
) {
}
