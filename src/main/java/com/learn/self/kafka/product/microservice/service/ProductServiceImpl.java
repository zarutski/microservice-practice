package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.core.ProductCreatedEvent;
import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ProductServiceImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final String TOPIC_NAME = "product-created-events-topic";

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductDTO createProductDTO) throws ExecutionException, InterruptedException {
        // TODO: save to DB, getId of created entity (random for now)
        String productId = UUID.randomUUID().toString();

        // form ProducerRecord (with messageKey, value, and headers)
        ProducerRecord<String, ProductCreatedEvent> record = formProductCreatedEventProducerRecord(createProductDTO, productId);
        // TODO: RETURN
        //  record.headers().add("messageId", UUID.randomUUID().toString().getBytes()); // messageId generation (for idempotent consumer support)
        record.headers().add("messageId", "UUID.randomUUID().toString()".getBytes()); // constant messageId - idempotent consumer skip processing testing

        // publish message via KafkaTemplate (wrapper around Kafka Producer provided by Spring) and retrieve result (sync mode)
        SendResult<String, ProductCreatedEvent> result = kafkaTemplate
                .send(record)
                .get(); // method get will stop current Thread processing (waiting for result)

        // RecordMetadata: kafka provided class for message metadata (offset, topic, partition ...)
        LOGGER.info("Topic {}", result.getRecordMetadata().topic());
        LOGGER.info("Partition {}", result.getRecordMetadata().partition());
        LOGGER.info("Offset {}", result.getRecordMetadata().offset());

        LOGGER.info("Return: {}", productId); // testing sync mode
        return productId;
    }

    private ProducerRecord<String, ProductCreatedEvent> formProductCreatedEventProducerRecord(CreateProductDTO createProductDTO, String productId) {
        // event creation (after entity is saved)
        ProductCreatedEvent createdEvent = new ProductCreatedEvent(productId, createProductDTO.getTitle(),
                createProductDTO.getPrice(), createProductDTO.getQuantity());

        // sending message via kafkaTemplate.send() internally creates a ProducerRecord (holds the message key, value, and headers)
        // headers of the producerRecord can carry additional metadata (e.g., messageId) without modifying the main payload
        return new ProducerRecord<>(TOPIC_NAME, productId, createdEvent);
    }

}
