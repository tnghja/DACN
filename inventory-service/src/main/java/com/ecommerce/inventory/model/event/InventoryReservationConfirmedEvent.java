package com.ecommerce.inventory.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReservationConfirmedEvent(/* extends Event */
        UUID eventId,
        String orderId,     // Changed to String
        LocalDateTime timestamp
) implements Event {
    public InventoryReservationConfirmedEvent(String orderId) {
        this(UUID.randomUUID(), orderId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "inventory-reservation-confirmed";
    }
}
