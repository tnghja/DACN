package com.ecommerce.order.config;

// Keep existing imports: Event, ConsumerConfig, ProducerConfig, serializers, etc.
import com.ecommerce.order.model.event.Event; // Assuming this is your base for some events
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;



    // --- FIXED Producer Configuration ---
    @Bean
    // FIX: Changed generic value type from Event to Object for flexibility
    public ProducerFactory<String, Object> orderProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Use JsonSerializer for values, it handles various object types
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // If events don't have type info headers embedded by default by JsonSerializer,
        // consumers might need specific default types or rely on payload structure.
        // configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); // Keep default usually
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    // FIX: Changed generic value type from Event to Object to match factory
    public KafkaTemplate<String, Object> orderKafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object>orderConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);


        // Configure ErrorHandlingDeserializer as the main deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // *** FIX: Use correct keys to configure the delegates ***
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class); // Use class directly or .getName()
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // Use class directly or .getName()

        // Configure JsonDeserializer properties (still recommend using application.properties)
        // These are passed to the JsonDeserializer when wrapped by ErrorHandlingDeserializer
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // !! Change to specific packages for production !!
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Explicitly use headers (default)

        // Only include type mappings if needed (i.e., separate event class definitions)
        // If using shared event module, REMOVE this mapping.
        String typeMappings = "com.ecommerce.inventory.model.event.ReserveInventoryRequestEvent:com.ecommerce.order.model.event.ReserveInventoryRequestEvent,"
                + "com.ecommerce.inventory.model.event.InventoryReservationConfirmedEvent:com.ecommerce.order.model.event.InventoryReservationConfirmedEvent,"
                + "com.ecommerce.inventory.model.event.InventoryReservationReleasedEvent:com.ecommerce.order.model.event.InventoryReservationReleasedEvent";
        props.put(JsonDeserializer.TYPE_MAPPINGS, typeMappings);

        // Remove the incorrect delegate properties from before
        // props.put("spring.deserializer.key.delegate.class", StringDeserializer.class.getName()); // REMOVED
        // props.put("spring.deserializer.value.delegate.class", JsonDeserializer.class.getName()); // REMOVED

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, Object>productListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);


        // Configure ErrorHandlingDeserializer as the main deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // *** FIX: Use correct keys to configure the delegates ***
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class); // Use class directly or .getName()
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // Use class directly or .getName()

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // !! Change to specific packages for production !!
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Explicitly use headers (default)
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.ecommerce.order.model.event.DebeziumProductEvent");
        // Remove the incorrect delegate properties from before
        // props.put("spring.deserializer.key.delegate.class", StringDeserializer.class.getName()); // REMOVED
        // props.put("spring.deserializer.value.delegate.class", JsonDeserializer.class.getName()); // REMOVED

        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    // Listener factory using the consumer factory above
    public ConcurrentKafkaListenerContainerFactory<String, Event> orderListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConsumerFactory());

        // Configure Error Handler (Same as before, good starting point)
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new FixedBackOff(1000L, 2L) // interval, maxAttempts
        ));

        return factory;
    }

    @Bean
    // Listener factory using the consumer factory above
    public ConcurrentKafkaListenerContainerFactory<String, Event> productListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productListenerFactory());

        // Configure Error Handler (Same as before, good starting point)
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new FixedBackOff(1000L, 2L) // interval, maxAttempts
        ));

        return factory;
    }
}