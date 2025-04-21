package com.ecommerce.inventory.configuration; // Example package

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
// Import JsonDeserializer to access its constants
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Consider fetching these via @Value as well for consistency
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") // Provide default
    private String autoOffsetReset;


    @Bean
    // Using Object as value type because type mapping can produce specific Event objects
    public ConsumerFactory<String, Object> inventoryConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset); // Use value from properties or default

        // Configure ErrorHandlingDeserializer wrapper
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Configure Delegates for ErrorHandlingDeserializer
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // Use JsonDeserializer as delegate

        // --- Configure the JsonDeserializer delegate ---
        // Trust all packages (consider restricting this in production)
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // *** ADD TYPE MAPPINGS HERE ***
        // Define the mapping from type info in message headers/payload to actual classes
        // Note: This is a long string, formatting with concatenation for readability
        String typeMappings = "com.ecommerce.order.model.event.ReserveInventoryRequestEvent:com.ecommerce.inventory.model.event.ReserveInventoryRequestEvent,"
                + "com.ecommerce.order.model.event.InventoryReservationConfirmedEvent:com.ecommerce.inventory.model.event.InventoryReservationConfirmedEvent,"
                + "com.ecommerce.order.model.event.InventoryReservationReleasedEvent:com.ecommerce.inventory.model.event.InventoryReservationReleasedEvent";
        props.put(JsonDeserializer.TYPE_MAPPINGS, typeMappings);

        // Tell JsonDeserializer to use type info headers (Spring Kafka default is true)
         props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Tell JsonDeserializer NOT to add type info headers for producer (consumer property)
        // props.put(JsonDeserializer.ADD_TYPE_INFO_HEADERS, false); // Default for consumer


        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    // Using Object as value type because type mapping can produce specific Event objects
    public ConsumerFactory<String, Object> productListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset); // Use value from properties or default

        // Configure ErrorHandlingDeserializer wrapper
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Configure Delegates for ErrorHandlingDeserializer
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // Use JsonDeserializer as delegate

        // --- Configure the JsonDeserializer delegate ---
        // Trust all packages (consider restricting this in production)
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Tell JsonDeserializer to use type info headers (Spring Kafka default is true)
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.ecommerce.inventory.model.event.DebeziumProductEvent");

        return new DefaultKafkaConsumerFactory<>(props);
    }
    @Bean
    // Match the value type with the factory
    public ConcurrentKafkaListenerContainerFactory<String, Object> productListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productListenerFactory());

        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new FixedBackOff(1000L, 2L) // Sensible retries
        ));

        return factory;
    }
    @Bean
    // Match the value type with the factory
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(inventoryConsumerFactory());

        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new FixedBackOff(1000L, 2L) // Sensible retries
        ));

        return factory;
    }
    // --- Producer config remains commented out ---
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Use JsonSerializer for the value
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Optional: Add type info header if the Order Service consumer expects it
        // configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // --- *** NEW: KafkaTemplate Bean *** ---
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}