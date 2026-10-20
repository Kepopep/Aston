package com.example.notification.kafka;

import com.example.common.event.UserEvent;
import com.example.notification.mail.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    private final EmailNotificationService notificationService;

    public UserEventListener(EmailNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = UserEvent.TOPIC)
    public void onUserEvent(UserEvent event) {
        logger.info("Получено событие из Kafka: {}", event);
        notificationService.sendNotification(event.operation(), event.email());
    }
}
