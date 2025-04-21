package com.ecommerce.inventory.model.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record ReserveInventoryRequestEvent (/* extends Event */
        UUID eventId,       // Unique ID for this event instance
        String orderId,     // Changed to String to match Reservation entity
        List<InventoryItemRequest> items,
        LocalDateTime timestamp
) implements Event
{
    public ReserveInventoryRequestEvent(String orderId, List<InventoryItemRequest> items) {
        this(UUID.randomUUID(), orderId, items, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "reserve-inventory-request";
    }
}