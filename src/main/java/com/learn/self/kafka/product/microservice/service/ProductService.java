package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;

import java.util.concurrent.ExecutionException;

public interface ProductService {

    String createProduct(CreateProductDTO createProductDTO) throws ExecutionException, InterruptedException;

}
