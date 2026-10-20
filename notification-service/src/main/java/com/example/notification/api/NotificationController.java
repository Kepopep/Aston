package com.example.notification.api;

import com.example.notification.mail.EmailNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailNotificationService notificationService;

    public NotificationController(EmailNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> send(@Valid @RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request.operation(), request.email());
        return ResponseEntity.ok().build();
    }
}
