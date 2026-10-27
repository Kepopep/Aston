package com.example.DTO;

import com.example.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Пользователь")
public record UserResponseDto(
        @Schema(description = "Идентификатор пользователя", example = "1")
        Long id,
        @Schema(description = "Имя пользователя", example = "Ivan")
        String name,
        @Schema(description = "Email пользователя", example = "ivan@example.com")
        String email,
        @Schema(description = "Возраст пользователя", example = "25")
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
