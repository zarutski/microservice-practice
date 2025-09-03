package com.learn.self.kafka.product.microservice.controller;

import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;
import com.learn.self.kafka.product.microservice.exception.ErrorMessage;
import com.learn.self.kafka.product.microservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        String productId = null;
        try {
            productId = productService.createProduct(createProductDTO);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(new Date(), e.getMessage())); // TODO: exception handling stub
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productId);
    }
}
