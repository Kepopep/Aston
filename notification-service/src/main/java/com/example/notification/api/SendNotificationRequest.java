package com.example.notification.api;

import com.example.common.event.UserOperation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
        @NotNull UserOperation operation,
        @NotBlank @Email String email
) {
}
