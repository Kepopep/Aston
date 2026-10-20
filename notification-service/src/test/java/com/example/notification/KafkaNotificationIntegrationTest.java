package com.example.notification;

import com.example.common.event.UserEvent;
import com.example.common.event.UserOperation;
import com.example.notification.mail.EmailNotificationService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaNotificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Test
    void userCreatedEvent_shouldSendCreatedEmail() throws Exception {
        kafkaTemplate.send(UserEvent.TOPIC, new UserEvent(UserOperation.CREATED, "new@mail.ru"));

        MimeMessage message = awaitSingleMessage();

        assertEquals("new@mail.ru", message.getAllRecipients()[0].toString());
        assertEquals("no-reply@test-site.ru", message.getFrom()[0].toString());
        assertEquals(EmailNotificationService.SUBJECT, message.getSubject());
        assertEquals(CREATED_TEXT, textOf(message));
    }

    @Test
    void userDeletedEvent_shouldSendDeletedEmail() throws Exception {
        kafkaTemplate.send(UserEvent.TOPIC, new UserEvent(UserOperation.DELETED, "old@mail.ru"));

        MimeMessage message = awaitSingleMessage();

        assertEquals("old@mail.ru", message.getAllRecipients()[0].toString());
        assertEquals(DELETED_TEXT, textOf(message));
    }
}
