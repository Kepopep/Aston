package com.example.common.event;

public record UserEvent(
        UserOperation operation,
        String email
) {
    public static final String TOPIC = "user-events";
}
