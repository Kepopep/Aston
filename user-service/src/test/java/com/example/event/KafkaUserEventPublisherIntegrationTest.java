package com.example.event;

import com.example.common.event.UserEvent;
import com.example.common.event.UserOperation;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = KafkaUserEventPublisher.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer",
                "spring.kafka.producer.properties.spring.json.add.type.headers=false"
        })
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = UserEvent.TOPIC)
class KafkaUserEventPublisherIntegrationTest {

    @Autowired
    private UserEventPublisher publisher;

    @Autowired
    private EmbeddedKafkaBroker broker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(broker, "test-group", false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer())
                .createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, UserEvent.TOPIC);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void publish_shouldSendEventWithOperationAndEmail() {
        publisher.publish(UserOperation.CREATED, "user@mail.ru");

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, UserEvent.TOPIC, Duration.ofSeconds(15));

        assertEquals("user@mail.ru", record.key());
        assertTrue(record.value().contains("\"operation\":\"CREATED\""), record.value());
        assertTrue(record.value().contains("\"email\":\"user@mail.ru\""), record.value());
    }
}
