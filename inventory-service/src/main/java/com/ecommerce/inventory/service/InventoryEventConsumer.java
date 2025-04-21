package com.ecommerce.inventory.service;

// --- Imports ---
import com.ecommerce.inventory.model.event.InventoryReservationConfirmedEvent;
import com.ecommerce.inventory.model.event.InventoryReservationReleasedEvent;
import com.ecommerce.inventory.model.event.ReserveInventoryRequestEvent;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler; // Import KafkaHandler
import org.springframework.kafka.annotation.KafkaListener; // Import KafkaListener
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
// NOTE: No @Payload needed for @KafkaHandler methods targeting specific types
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
// 1. Add @KafkaListener at the CLASS level
@KafkaListener(topics = "${app.kafka.topics.inventory-requests}",
        groupId = "inventory-group",
        containerFactory = "inventoryListenerContainerFactory") // Reference the factory
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    // NOTE: ObjectMapper injection is no longer needed here

    // 2. Create specific handlers for each event type

    @KafkaHandler
    public void handleReservationRequest(ReserveInventoryRequestEvent event, // Method parameter IS the specific type
                                         @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                         @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Handling ReserveInventoryRequestEvent [Key: {}, Offset: {}]: {}", key, offset, event);
        try {
            inventoryService.reserveInventory(event.orderId(), event.items(), event.eventId());
            log.info("Successfully processed reservation request for orderId: {}", event.orderId());
        } catch (InsufficientStockException e) {
            log.error("Inventory reservation failed for orderId: {}. Reason: {}", event.orderId(), e.getMessage());
            // TODO: Send failure feedback event
        } catch (Exception e) {
            log.error("Error processing reservation request for orderId: {}: {}", event.orderId(), e.getMessage(), e);
            throw new RuntimeException("Failed processing ReserveInventoryRequestEvent for orderId " + event.orderId(), e);
        }
    }

    @KafkaHandler
    public void handleConfirmation(InventoryReservationConfirmedEvent event, // Method parameter IS the specific type
                                   @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Handling InventoryReservationConfirmedEvent [Key: {}, Offset: {}]: {}", key, offset, event);
        try {
            inventoryService.confirmReservation(event.orderId(), event.eventId());
            log.info("Successfully processed confirmation for orderId: {}", event.orderId());
        } catch (NotFoundException e) {
            log.warn("Reservation not found during confirmation for orderId: {}. Reason: {}", event.orderId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error processing confirmation for orderId: {}: {}", event.orderId(), e.getMessage(), e);
            throw new RuntimeException("Failed processing InventoryReservationConfirmedEvent for orderId " + event.orderId(), e);
        }
    }

    @KafkaHandler
    public void handleRelease(InventoryReservationReleasedEvent event, // Method parameter IS the specific type
                              @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Handling InventoryReservationReleasedEvent [Key: {}, Offset: {}]: {}", key, offset, event);
        try {
            inventoryService.releaseReservation(event.orderId(), event.eventId());
            log.info("Successfully processed release for orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Error processing release for orderId: {}: {}", event.orderId(), e.getMessage(), e);
            throw new RuntimeException("Failed processing InventoryReservationReleasedEvent for orderId " + event.orderId(), e);
        }
    }

    // Optional default handler
    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object payload,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        log.warn("Received unhandled payload type [Key: {}, Offset: {}]: Type: {}", key, offset,
                (payload != null ? payload.getClass().getName() : "null"));
    }
}