package com.example.event;

import com.example.common.event.UserOperation;

public interface UserEventPublisher {
    void publish(UserOperation operation, String email);
}
