package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;
import com.learn.self.kafka.product.microservice.service.event.ProductCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

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

        // publish message via KafkaTemplate (wrapper around Kafka Producer provided by Spring) - async mode (by default)
        kafkaTemplate.send("payment-created-events-topic", productId, createdEvent);

        // TODO: async response processing
        return "";
    }

}
