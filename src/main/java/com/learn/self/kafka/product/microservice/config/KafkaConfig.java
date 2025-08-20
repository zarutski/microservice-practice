package com.learn.self.kafka.product.microservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    // describe bean to create new topic at the application startup
    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name("product-created-events-topic")
                .partitions(3)
                .replicas(3) // each partition will be stored on 3 brokers (1 leader + 2 followers); max possible if the cluster has 3 brokers
                .configs(Map.of("min.insync.replicas", "2")) // additional property: minimum number of in-sync/synchronized replicas (including leader) that must acknowledge the message
                .build();
    }

}
