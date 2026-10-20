package com.example.event;

import com.example.common.event.UserEvent;
import com.example.common.event.UserOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaUserEventPublisher implements UserEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaUserEventPublisher.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public KafkaUserEventPublisher(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(UserOperation operation, String email) {
        UserEvent event = new UserEvent(operation, email);
        kafkaTemplate.send(UserEvent.TOPIC, email, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("Не удалось отправить событие {} в Kafka", event, ex);
                    } else {
                        logger.info("Событие {} отправлено в Kafka", event);
                    }
                });
    }
}
