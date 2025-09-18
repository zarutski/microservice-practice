package com.learn.self.kafka.product.microservice;

import com.learn.self.kafka.product.core.ProductCreatedEvent;
import com.learn.self.kafka.product.microservice.dto.CreateProductDTO;
import com.learn.self.kafka.product.microservice.service.ProductService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
// starts an embedded Kafka cluster for testing
@EmbeddedKafka(
        partitions = 3,        // number of partitions for the *default* topic
        count = 3,             // number of brokers in the cluster
        controlledShutdown = true // orderly broker shutdown (waits for all processes to finish)
)
// Integration test: SpringBootTest sets up the Spring Context, value spring.embedded.kafka.brokers - use embedded kafka for testing
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private Environment environment;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaMessageListenerContainer<String, ProductCreatedEvent> container;

    private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records;

    // this block sets up a working consumer in the test environment that listens to the topic and collects all messages for verification
    @BeforeAll
    public void setUp() {
        /*
           First, a consumer factory is created with the required settings, then a ContainerProperties object tells the container which topic to listen to.
           After that, a KafkaMessageListenerContainer is created — this is the main mechanism that starts the consumers and receives messages.
           All incoming events are placed into a LinkedBlockingQueue so the test can inspect them. Finally, ContainerTestUtils.waitForAssignment
           waits until the container has been assigned all partitions of the topic, ensuring the consumer does not miss any messages.
         */
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class, // wraps actual deserializer, allowing to handle deserialization errors (can skip or route bad messages)
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class, // actual deserializer
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"), // set the consumer group ID used to join a specific consumer group
                JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
    }

    @Test
    public void testCreateProductValidDetailsSuccess() throws ExecutionException, InterruptedException {
        String title = "Samsung";
        BigDecimal price = new BigDecimal(600);
        Integer quantity = 1;
        CreateProductDTO productDTO = new CreateProductDTO(title, price, quantity);

        productService.createProduct(productDTO);

        // #createProduct results check
        ConsumerRecord<String, ProductCreatedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertNotNull(message.key());
        ProductCreatedEvent createdEvent = message.value();
        assertEquals(productDTO.getQuantity(), createdEvent.getQuantity());
        assertEquals(productDTO.getTitle(), createdEvent.getTitle());
        assertEquals(productDTO.getPrice(), createdEvent.getPrice());
    }

    // TODO - increase coverage (negative scenarios add)

    @AfterAll
    public void teaDown() {
        container.stop();
    }

}
