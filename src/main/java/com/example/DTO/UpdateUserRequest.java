package com.example.DTO;

public record UpdateUserRequest(
        String name,
        String email,
        Integer age
) {
}
