package com.ecommerce.order.model.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Event indicating reservation failure (e.g., insufficient stock)
public record InventoryReservationFailedEvent(
        UUID eventId,
        String orderId,
        String reason, // Reason for failure
        List<InventoryItemRequest> items, // Optional: include items that failed
        LocalDateTime timestamp
) implements Event {
    public InventoryReservationFailedEvent(String orderId, String reason, List<InventoryItemRequest> items) {
        this(UUID.randomUUID(), orderId, reason, items, LocalDateTime.now());
    }

    public InventoryReservationFailedEvent(String orderId, String reason) {
        this(UUID.randomUUID(), orderId, reason, null, LocalDateTime.now());
    }


    @Override
    public String eventType() {
        return "inventory-reservation-failed";
    }
}