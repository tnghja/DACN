package com.ecommerce.inventory.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReservationReleasedEvent(/* extends Event */
        UUID eventId,
        String orderId,    // Changed to String
        LocalDateTime timestamp
) implements Event {
    public InventoryReservationReleasedEvent(String orderId) {
        this(UUID.randomUUID(), orderId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "inventory-reservation-released";
    }
}
