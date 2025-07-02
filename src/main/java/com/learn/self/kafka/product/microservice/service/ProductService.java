package com.learn.self.kafka.product.microservice.service;

import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;

public interface ProductService {

    String createProduct(CreateProductDTO createProductDTO);

}
