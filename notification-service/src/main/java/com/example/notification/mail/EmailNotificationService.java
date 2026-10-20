package com.example.notification.mail;

import com.example.common.event.UserOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    public static final String SUBJECT = "Уведомление об аккаунте";

    private final JavaMailSender mailSender;
    private final String from;
    private final String siteName;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.site-name}") String siteName
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.siteName = siteName;
    }

    public void sendNotification(UserOperation operation, String email) {
        if (operation == null) {
            throw new IllegalArgumentException("Не указана операция");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Не указан email");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(SUBJECT);
        message.setText(buildText(operation));

        mailSender.send(message);
        logger.info("Письмо об операции {} отправлено на {}", operation, email);
    }

    public String buildText(UserOperation operation) {
        return switch (operation) {
            case CREATED -> "Здравствуйте! Ваш аккаунт на сайте " + siteName + " был успешно создан.";
            case DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
