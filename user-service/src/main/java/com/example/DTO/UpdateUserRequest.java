package com.example.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на частичное обновление пользователя; незаданные поля не меняются")
public record UpdateUserRequest(
        @Schema(description = "Новое имя пользователя", example = "Ivan", nullable = true)
        String name,
        @Schema(description = "Новый email пользователя", example = "ivan@example.com", nullable = true)
        String email,
        @Schema(description = "Новый возраст пользователя", example = "26", nullable = true)
        Integer age
) {
}
