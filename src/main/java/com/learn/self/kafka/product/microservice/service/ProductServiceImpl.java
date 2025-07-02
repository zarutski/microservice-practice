package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;
import com.learn.self.kafka.product.microservice.service.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductDTO createProductDTO) {
        // TODO: save to DB, getId of created entity (random for now)
        String productId = UUID.randomUUID().toString();

        // event creation (after entity is saved)
        ProductCreatedEvent createdEvent = new ProductCreatedEvent(productId, createProductDTO.getTitle(),
                createProductDTO.getPrice(), createProductDTO.getQuantity());

        // publish message via KafkaTemplate (wrapper around Kafka Producer provided by Spring) - async mode (by default) and retrieve result (CompletableFuture usage - async)
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send("payment-created-events-topic", productId, createdEvent);

        // async response processing
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("Failed to send message: {}", ex.getMessage());
            } else {
                LOGGER.info("Messege send successfully: {}", result.getRecordMetadata()); // metadata here: kafka provided class for message metadata (offset, topic, partition ...)
            }
        });

        LOGGER.info("Return: {}", productId); // testing async mode (this log will appear before any callback log is printed)
        return productId;
    }

}
