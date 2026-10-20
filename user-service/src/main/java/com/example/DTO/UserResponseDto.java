package com.example.DTO;

import com.example.User;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Integer age
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge()
        );
    }
}
