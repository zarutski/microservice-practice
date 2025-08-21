package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.core.ProductCreatedEvent;
import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;

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

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductDTO createProductDTO) throws ExecutionException, InterruptedException {
        // TODO: save to DB, getId of created entity (random for now)
        String productId = UUID.randomUUID().toString();

        // event creation (after entity is saved)
        ProductCreatedEvent createdEvent = new ProductCreatedEvent(productId, createProductDTO.getTitle(),
                createProductDTO.getPrice(), createProductDTO.getQuantity());

        // publish message via KafkaTemplate (wrapper around Kafka Producer provided by Spring) and retrieve result (sync mode)
        SendResult<String, ProductCreatedEvent> result = kafkaTemplate
                .send("product-created-events-topic", productId, createdEvent)
                .get(); // method get will stop current Thread processing (waiting for result)

        // RecordMetadata: kafka provided class for message metadata (offset, topic, partition ...)
        LOGGER.info("Topic {}", result.getRecordMetadata().topic());
        LOGGER.info("Partition {}", result.getRecordMetadata().partition());
        LOGGER.info("Offset {}", result.getRecordMetadata().offset());

        LOGGER.info("Return: {}", productId); // testing sync mode
        return productId;
    }

}
