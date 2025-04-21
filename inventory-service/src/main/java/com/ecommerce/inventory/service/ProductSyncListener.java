package com.ecommerce.inventory.service; // Or an appropriate package

import com.ecommerce.inventory.exception.ValidationException;
import com.ecommerce.inventory.model.entity.Product;
// Ensure you import the CORRECT DebeziumProductEvent POJO (the one with the nested Payload)
import com.ecommerce.inventory.model.event.DebeziumProductEvent;
import com.ecommerce.inventory.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects; // Keep this import

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSyncListener {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.dbz-products}",
            // Use the appropriate groupId (ideally configured via factory)
             groupId = "product-sync-group", // Remove if groupId is set in factory
            containerFactory = "productListenerContainerFactory"
    )
    @Transactional
    public void handleProductChangeEvent(@Payload(required = false) DebeziumProductEvent event) {
        // --- *** UPDATED: Check event AND payload *** ---
        if (event == null || event.getPayload() == null) {
            log.warn("Received null message or message without payload on products CDC topic. Skipping.");
            return;
        }

        // --- *** UPDATED: Get nested payload *** ---
        DebeziumProductEvent.Payload payload = event.getPayload();
        DebeziumProductEvent.Source source = payload.getSource(); // Get source from payload

        // Basic null check for source, though payload null check mostly covers it
        if (source == null) {
            log.warn("Received message without source information within payload. Skipping.");
            return;
        }

        String sourceTable = source.getTable();
        if (!"product".equalsIgnoreCase(sourceTable)) {
            log.warn("Listener received event for unexpected table '{}'. Expected 'products'. Skipping.", sourceTable);
            return;
        }

        // --- *** UPDATED: Access op and tsMs from payload *** ---
        String operation = payload.getOp();
        Long timestamp = payload.getTsMs();

        log.info("Received Product CDC Event | Op: {} | Table: {} | Timestamp: {}",
                operation, sourceTable, timestamp);

        try {
            // --- *** UPDATED: Access before/after from payload *** ---
            Map<String, Object> beforeData = payload.getBefore();
            Map<String, Object> afterData = payload.getAfter();

            if ("d".equals(operation)) {
                // --- Handle DELETE ---
                if (beforeData != null) {
                    String productId = extractProductId(beforeData, true); // ID is mandatory for delete context
                    // Remainder of DELETE logic remains the same, using productId...
                    if (productId != null) {
                        log.info("Processing DELETE for Product ID: {}", productId);
                        productRepository.findById(productId).ifPresent(product -> {
                            if (product.getDeleteAt() == null) {
                                product.setDeleteAt(LocalDateTime.now());
                                productRepository.save(product);
                                log.info("Soft deleted Product ID: {}", productId);
                            } else {
                                log.warn("Product ID {} was already soft deleted at {}. Ignoring delete event.", productId, product.getDeleteAt());
                            }
                        });
                        if (!productRepository.existsById(productId)) {
                            log.warn("Product ID {} not found locally during DELETE processing.", productId);
                        }
                    }
                } else {
                    log.warn("Received DELETE event with null 'before' data. Cannot process delete without ID.");
                    throw new ValidationException("Cannot process DELETE event: 'before' data with product ID is missing.");
                }

            } else if ("c".equals(operation) || "u".equals(operation) || "r".equals(operation)) {
                // --- Handle CREATE, UPDATE, READ (Snapshot) ---
                if (afterData != null) {
                    String productId = extractProductId(afterData, true);
                    // Remainder of C/U/R logic remains the same, using productId and afterData...
                    if (productId != null) {
                        log.info("Processing {} for Product ID: {}", operation.toUpperCase(), productId);

                        Product product = productRepository.findById(productId)
                                .orElse(new Product()); // Create new if not found

                        mapDataToProduct(afterData, product);
                        handleSourceSoftDelete(afterData, product);
                        productRepository.save(product);
                        log.info("Saved/Updated Product ID: {}", productId);
                    }
                } else {
                    log.warn("Received C/U/R event with null 'after' data for operation '{}'. Cannot process.", operation);
                    throw new ValidationException("Cannot process " + operation.toUpperCase() + " event: 'after' data is missing.");
                }
            } else {
                log.warn("Received event with unhandled operation type: {}", operation);
            }

        } catch (ValidationException ve) {
            // Log validation errors specifically
            // --- *** UPDATED: Access op from variable *** ---
            log.error("Validation Error processing Debezium Product event [Op: {}]: {}", operation, ve.getMessage());
            throw ve;
        } catch (Exception e) {
            // --- *** UPDATED: Access op from variable *** ---
            log.error("Error processing Debezium Product event [Op: {}]: {}", operation, e.getMessage(), e);
            throw new RuntimeException("Failed to process Debezium Product event", e);
        }
    }

    // --- Helper Methods (No changes needed in helpers themselves) ---

    private String extractProductId(Map<String, Object> data, boolean isMandatory) {
        if (data == null) {
            if (isMandatory) {
                throw new ValidationException("Product ID is mandatory but data map is null.");
            }
            return null;
        }
        Object idObj = data.get("id"); // Adjust if source column name is different
        String productId = (idObj != null) ? String.valueOf(idObj) : null;

        if (productId == null && isMandatory) {
            throw new ValidationException("Product ID is mandatory but missing or null in event data.");
        }
        return productId;
    }

    private void mapDataToProduct(Map<String, Object> data, Product product) {
        if (product.getId() == null) {
            String productId = extractProductId(data, true);
            product.setId(productId);
        }
        // Adjust map keys ("name", "price" etc.) if source column names differ
        product.setName(getString(data, "name", true));
        product.setPrice(getDouble(data, "price", true));
        product.setQuantity(getInteger(data, "quantity", true));
        product.setCategoryId(getLong(data, "category_id", true)); // Adjust key if needed

        product.setBrand(getString(data, "brand", false));
        product.setCover(getString(data, "cover", false));
        product.setDescription(getString(data, "description", false));
        product.setRate(getDouble(data, "rate", false));
    }

    private void handleSourceSoftDelete(Map<String, Object> data, Product product) {
        // Adjust map key "deleteAt" if source column name is different
        Object deleteAtObj = data.get("deleteAt");

        if (deleteAtObj != null) {
            LocalDateTime deleteTimestamp = convertToLocalDateTime(deleteAtObj);
            if (deleteTimestamp != null && product.getDeleteAt() == null) {
                log.info("Source event indicates soft delete for Product ID {}. Setting deleteAt.", product.getId());
                product.setDeleteAt(deleteTimestamp);
            }
        } else if (product.getDeleteAt() != null) {
            log.info("Source event has no delete indicator for Product ID {}. Resetting local deleteAt.", product.getId());
            product.setDeleteAt(null);
        }
    }

    // --- Safe Type Conversion Helpers (Remain the same) ---

    private String getString(Map<String, Object> data, String key, boolean isMandatory) {
        Object value = data.get(key);
        String stringValue = (value != null) ? String.valueOf(value) : null;
        if (stringValue == null && isMandatory) {
            throw new ValidationException(String.format("Mandatory field '%s' is missing or null in event data.", key));
        }
        return stringValue;
    }

    private Double getDouble(Map<String, Object> data, String key, boolean isMandatory) {
        Object value = data.get(key);
        Double doubleValue = null;
        if (value instanceof Number) {
            doubleValue = ((Number) value).doubleValue();
        } else if (value != null) {
            try {
                doubleValue = Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException e) {
                log.warn("Could not parse Double from key '{}', value: {}", key, value);
            }
        }
        if (doubleValue == null && isMandatory) {
            throw new ValidationException(String.format("Mandatory field '%s' is missing, null, or not a valid number in event data.", key));
        }
        return doubleValue;
    }

    private Integer getInteger(Map<String, Object> data, String key, boolean isMandatory) {
        Object value = data.get(key);
        Integer intValue = null;
        if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else if (value != null) {
            try {
                intValue = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException e) {
                log.warn("Could not parse Integer from key '{}', value: {}", key, value);
            }
        }
        if (intValue == null && isMandatory) {
            throw new ValidationException(String.format("Mandatory field '%s' is missing, null, or not a valid integer in event data.", key));
        }
        return intValue;
    }

    private Long getLong(Map<String, Object> data, String key, boolean isMandatory) {
        Object value = data.get(key);
        Long longValue = null;
        if (value instanceof Number) {
            longValue = ((Number) value).longValue();
        } else if (value != null) {
            try {
                longValue = Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException e) {
                log.warn("Could not parse Long from key '{}', value: {}", key, value);
            }
        }
        if (longValue == null && isMandatory) {
            throw new ValidationException(String.format("Mandatory field '%s' is missing, null, or not a valid long integer in event data.", key));
        }
        return longValue;
    }

    private LocalDateTime convertToLocalDateTime(Object timestampObj) {
        // --- UPDATED: Handle Debezium MicroTimestamp (Long) ---
        if (timestampObj instanceof Long) {
            // Debezium MicroTimestamp is microseconds since epoch
            long epochMicro = (Long) timestampObj;
            long epochMilli = epochMicro / 1000; // Convert to milliseconds
            // Use appropriate ZoneOffset, assuming UTC for epoch
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
        } else if (timestampObj instanceof Number) { // Handle other potential numeric types (e.g., epoch millis)
            long epochMilli = ((Number) timestampObj).longValue();
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
        }
        // Add parsing for String formats if necessary
        log.warn("Cannot convert timestamp object of type {} to LocalDateTime: {}",
                (timestampObj != null ? timestampObj.getClass().getName() : "null"), timestampObj);
        return null;
    }
}