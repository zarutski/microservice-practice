package com.learn.self.kafka.product.microservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic createTopic() {
        return TopicBuilder.name("payment-created-events-topic")
                .partitions(3)
                .replicas(3) // 3 topic replicas is maximum count can be achieved (one leader, two followers), because cluster is configured with 3 server/brokers via CLI
                .configs(Map.of("min.insync.replicas", "2")) // additional property: sets minimal count of synchronized server/brokers (at least two servers should be synchronized with each other)
                .build();
    }

}
