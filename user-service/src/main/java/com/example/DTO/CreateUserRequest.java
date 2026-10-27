package com.example.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание пользователя")
public record CreateUserRequest(
        @Schema(description = "Имя пользователя", example = "Ivan")
        String name,
        @Schema(description = "Email пользователя", example = "ivan@example.com")
        String email,
        @Schema(description = "Возраст пользователя", example = "25")
        Integer age
) {
}
