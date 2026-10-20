package com.example.notification;

import com.example.common.event.UserEvent;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Поднимает встроенную Kafka и встроенный SMTP-сервер (GreenMail),
 * поэтому тесты не требуют Docker или внешних сервисов.
 */
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "app.site-name=test-site.ru",
        "app.mail.from=no-reply@test-site.ru"
})
@EmbeddedKafka(partitions = 1, topics = UserEvent.TOPIC)
@DirtiesContext
public abstract class AbstractIntegrationTest {

    @RegisterExtension
    protected static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication())
            .withPerMethodLifecycle(true);

    protected static final String CREATED_TEXT =
            "Здравствуйте! Ваш аккаунт на сайте test-site.ru был успешно создан.";
    protected static final String DELETED_TEXT =
            "Здравствуйте! Ваш аккаунт был удалён.";

    protected MimeMessage awaitSingleMessage() {
        greenMail.waitForIncomingEmail(15_000, 1);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length, "Ожидалось ровно одно письмо");
        return messages[0];
    }

    protected static String textOf(MimeMessage message) throws Exception {
        return message.getContent().toString().trim();
    }
}
