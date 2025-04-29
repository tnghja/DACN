package com.ecommerce.inventory.model.event; // Or com.ecommerce.order.model.cdc if in order-service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents the structure of a Debezium change event (with schema envelope)
 * for the 'products' table.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore the top-level "schema" field and any other unknowns
public class DebeziumProductEvent {

    // This field will hold the nested payload object from the JSON message
    private Payload payload;

    // --- Nested Payload Class ---
    // This class now contains the fields previously at the top level
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {

        // Row state BEFORE the change (Map is suitable for schema-less value part)
        private Map<String, Object> before;

        // Row state AFTER the change (Map is suitable for schema-less value part)
        private Map<String, Object> after;

        // Source metadata object
        private Source source;

        // Operation type: 'c', 'u', 'd', 'r'
        private String op;

        // Timestamp from the event envelope (milliseconds)
        @JsonProperty("ts_ms")
        private Long tsMs;

        // Transaction metadata block (optional)
        private Transaction transaction; // Use specific class if structure is fixed, otherwise Map

    }

    // --- Nested Source Class ---
    // (Updated with fields seen in the example schema)
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String version;
        private String connector;
        private String name;
        @JsonProperty("ts_ms")
        private Long tsMsSource; // Renamed slightly to avoid collision if needed, though not strictly necessary here
        private String snapshot;
        private String db;
        private String sequence;
        @JsonProperty("ts_us") // Added based on schema
        private Long tsUs;
        @JsonProperty("ts_ns") // Added based on schema
        private Long tsNs;
        private String schema;
        private String table;
        @JsonProperty("txId")
        private Long txId;
        private Long lsn;
        private Long xmin; // Added based on schema (even if null in payload example)
    }

    // --- Nested Transaction Class ---
    // (Based on the schema definition in the example message)
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        private String id;
        @JsonProperty("total_order")
        private Long totalOrder;
        @JsonProperty("data_collection_order")
        private Long dataCollectionOrder;
    }
}