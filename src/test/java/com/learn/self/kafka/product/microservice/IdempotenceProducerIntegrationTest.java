package com.learn.self.kafka.product.microservice;

import com.learn.self.kafka.product.core.ProductCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class IdempotenceProducerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    /*
        @MockitoBean is used to safely verify producer settings (idempotence, acks, retries)
        without creating topics or altering Kafka broker state.

        KafkaAdmin is a Spring bean created automatically with KafkaTemplate or KafkaAutoConfiguration
        during SpringBootTest with EmbeddedKafka, it may try to create topics in the broker,
        which can slow down the test or cause errors if not mocked

        using @MockitoBean replaces the real KafkaAdmin with a mock so that:
        - The test does not create topics in the broker.
        - Producer configuration can be verified safely without side effects.
    */
    @MockitoBean
    private KafkaAdmin kafkaAdmin;

    private static final String ENABLE_IDEMPOTENCE = "true";

    private static final String ACK_CONFIG = "all";

    @Test
    void testProducerConfigIdempotenceEnabled() {
        ProducerFactory<String, ProductCreatedEvent> producerFactory = kafkaTemplate.getProducerFactory();

        Map<String, Object> configurationProperties = producerFactory.getConfigurationProperties();

        assertEquals(ENABLE_IDEMPOTENCE, configurationProperties.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        assertTrue(ACK_CONFIG.equalsIgnoreCase((String) configurationProperties.get(ProducerConfig.ACKS_CONFIG)));

        if (configurationProperties.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            assertTrue(Integer.parseInt(configurationProperties.get(ProducerConfig.RETRIES_CONFIG).toString()) > 0);
        }

        if (configurationProperties.containsKey(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)) {
            assertTrue(Integer.parseInt(configurationProperties.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION).toString()) <= 5);
        }
    }

}
