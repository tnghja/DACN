package com.ecommerce.inventory.service;

import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.NotFoundException;
import com.ecommerce.inventory.model.entity.*;
import com.ecommerce.inventory.model.event.InventoryItemRequest;
import com.ecommerce.inventory.model.event.InventoryReservationConfirmedEvent;
import com.ecommerce.inventory.model.event.InventoryReservationFailedEvent;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.ReservationRepository;
import com.ecommerce.inventory.repository.StockAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException; // Import this

import java.time.LocalDateTime;
import java.time.Duration; // For reservation expiry
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final StockAuditLogRepository stockAuditLogRepository; // Assuming you have this
    private static final Duration RESERVATION_DURATION = Duration.ofMinutes(15); // Configurable

    @Value("${app.kafka.topics.order-feedback}") // Inject topic name
    private String orderFeedbackTopic;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    /**
     * Attempts to reserve inventory for a given order.
     * Throws InsufficientStockException if stock is not available.
     * Handles optimistic locking failures.
     *
     * @param orderId The ID of the order requesting reservation.
     * @param items   The list of items and quantities to reserve.
     * @param eventId Unique ID of the triggering event for idempotency check.
     */
    @Transactional(rollbackFor = Exception.class)
    public void reserveInventory(String orderId, List<InventoryItemRequest> items, UUID eventId) throws InsufficientStockException {

        if (reservationRepository.existsByOrderId(orderId)) {
            log.warn("Reservation attempt for already processed orderId: {}. EventId: {}. Skipping.", orderId, eventId);
            return; // Already processed or being processed
        }
        
        log.info("Attempting reservation for orderId: {}, eventId: {}", orderId, eventId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plus(RESERVATION_DURATION);

        Map<String, Integer> requestedQuantities = items.stream()
                .collect(Collectors.toMap(InventoryItemRequest::productId, InventoryItemRequest::quantity));

        try {
            for (InventoryItemRequest item : items) {
                String productId = item.productId();
                int quantityNeeded = item.quantity();

                try {
                    Inventory inventory = inventoryRepository.findById(productId)
                            .orElseThrow(() -> new InsufficientStockException("Product not found in inventory: " + productId));

                    int available = inventory.getAvailableQuantity();
                    int reserved = inventory.getReservedQuantity();
                    int previousAvailable = available;
                    int previousReserved = reserved;

                    if (available < quantityNeeded) {
                        log.warn("Insufficient stock for productId: {}. Needed: {}, Available: {}. OrderId: {}",
                                productId, quantityNeeded, available, orderId);
                        throw new InsufficientStockException("Insufficient stock for productId: " + productId);
                    }

                    inventory.reserveStock(quantityNeeded);
                    inventoryRepository.save(inventory);

                    // Create Audit Log (Assuming StockAuditLog constructor/setters are fixed)
                    StockAuditLog auditLog = new StockAuditLog();
                    auditLog.setProductId(productId);
                    auditLog.setOrderId(orderId);
                    auditLog.setReservationId(null); // Not known yet
                    auditLog.setChangeType(StockChangeType.RESERVATION_CREATED);
                    auditLog.setQuantityChange(-quantityNeeded); // Reflects change in available stock if that's the logic
                    auditLog.setPreviousAvailableQuantity(previousAvailable);
                    auditLog.setNewAvailableQuantity(inventory.getAvailableQuantity());
                    auditLog.setPreviousReservedQuantity(previousReserved);
                    auditLog.setNewReservedQuantity(inventory.getReservedQuantity());
                    auditLog.setReason("Reservation for order " + orderId);
                    // timestamp is set by @CreationTimestamp
                    stockAuditLogRepository.save(auditLog);


                } catch (OptimisticLockingFailureException e) {
                    log.error("Optimistic lock failed during reservation for productId: {}. OrderId: {}. Retrying may be needed.", productId, orderId, e);
                    throw new RuntimeException("Inventory conflict, please retry", e); // Let outer catch handle feedback
                }
                // InsufficientStockException is re-thrown below
                catch (Exception e) { // Catch other specific DB/unexpected errors if needed
                    log.error("Unexpected error during reservation for productId: {}. OrderId: {}", productId, orderId, e);
                    throw new RuntimeException("Failed to process reservation for product " + productId, e); // Let outer catch handle feedback
                }
            }

            // Create Reservation records
            for (InventoryItemRequest item : items) {
                Reservation reservation = new Reservation();
                reservation.setOrderId(orderId);
                reservation.setProductId(item.productId());
                reservation.setQuantity(item.quantity());
                reservation.setStatus(ReservationStatus.PENDING);
                reservation.setExpiresAt(expiryTime);
                reservationRepository.save(reservation);
                log.info("Created reservation record for orderId: {}, productId: {}", orderId, item.productId());
            }

            // *** Send SUCCESS feedback ***
            InventoryReservationConfirmedEvent successEvent = new InventoryReservationConfirmedEvent(orderId);
            log.info("Sending InventoryReservedEvent for orderId: {} to topic {}", orderId, orderFeedbackTopic);
            kafkaTemplate.send(orderFeedbackTopic, orderId, successEvent); // Use orderId as key

            log.info("Inventory successfully reserved for orderId: {}", orderId);

        } catch (InsufficientStockException e) {
            log.error("Inventory reservation failed for orderId: {}. Reason: {}", orderId, e.getMessage());
            // *** Send FAILURE feedback for insufficient stock ***
            InventoryReservationFailedEvent failureEvent = new InventoryReservationFailedEvent(orderId, e.getMessage(), items);
            log.info("Sending InventoryReservationFailedEvent (Insufficient Stock) for orderId: {} to topic {}", orderId, orderFeedbackTopic);
            kafkaTemplate.send(orderFeedbackTopic, orderId, failureEvent);
            // Do NOT re-throw here if the consumer shouldn't retry; the feedback is sent.
            // If the consumer *should* retry (e.g., temporary issue), re-throwing might be needed.
            // For insufficient stock, usually no retry is needed.

        } catch (Exception e) { // Catch other exceptions from the main try block
            log.error("General error during inventory reservation for orderId: {}: {}", orderId, e.getMessage(), e);
            // *** Send FAILURE feedback for general errors ***
            InventoryReservationFailedEvent failureEvent = new InventoryReservationFailedEvent(orderId, "General processing error: " + e.getMessage(), items);
            log.info("Sending InventoryReservationFailedEvent (General Error) for orderId: {} to topic {}", orderId, orderFeedbackTopic);
            kafkaTemplate.send(orderFeedbackTopic, orderId, failureEvent);
            // Decide whether to re-throw based on whether retries make sense for this error
            // throw new RuntimeException("Failed processing reservation request for orderId " + orderId, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmReservation(String orderId, UUID eventId) throws NotFoundException {
        log.info("Attempting to confirm reservation for orderId: {}, eventId: {}", orderId, eventId);

        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            log.warn("No reservations found for orderId: {} to confirm. EventId: {}. May have been cancelled/expired or never existed.", orderId, eventId);
            // Depending on requirements, might throw an error or just log and return.
            // Throwing ensures visibility if the order *should* have had reservations.
            throw new NotFoundException("No reservations found for orderId: " + orderId);
        }

        for (Reservation reservation : reservations) {
            // Idempotency: Only confirm PENDING reservations
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                log.warn("Reservation for orderId: {}, productId: {} is not PENDING (status: {}). Skipping confirmation. EventId: {}",
                        orderId, reservation.getProductId(), reservation.getStatus(), eventId);
                continue; // Move to the next item in the order
            }

            try {
                Inventory inventory = inventoryRepository.findById(reservation.getProductId())
                        .orElseThrow(() -> {
                            log.error("Inventory record not found for productId {} during confirmation of order {}", reservation.getProductId(), orderId);
                            // This is a serious data inconsistency
                            return new IllegalStateException("Inventory missing for product: " + reservation.getProductId());
                        });

                int previousReserved = inventory.getReservedQuantity(); // For audit

                // Update inventory: decrease reserved count
                inventory.confirmSale(reservation.getQuantity()); // Method adjusts reserved count

                // Save inventory - handle optimistic lock
                inventoryRepository.save(inventory);

                // Update reservation status
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);

                // Create Audit Log
                StockAuditLog auditLog = new StockAuditLog(
                        inventory.getProductId(), orderId, reservation.getReservationId(),
                        StockChangeType.OUTBOUND_SALE, // Or RESERVATION_CONFIRMED if you prefer
                        -reservation.getQuantity(), // Change in reserved stock
                        inventory.getAvailableQuantity(), inventory.getAvailableQuantity(), // Available unchanged here
                        previousReserved, inventory.getReservedQuantity(),
                        "Sale confirmed for order " + orderId
                );
                stockAuditLogRepository.save(auditLog);


                log.info("Confirmed reservation for orderId: {}, productId: {}", orderId, reservation.getProductId());

            } catch (OptimisticLockingFailureException e) {
                log.error("Optimistic lock failed during confirmation for productId: {}. OrderId: {}. Retrying may be needed.", reservation.getProductId(), orderId, e);
                throw new RuntimeException("Inventory conflict during confirmation, please retry", e);
            } catch (Exception e) {
                log.error("Failed to confirm reservation for orderId: {}, productId: {}. Error: {}", orderId, reservation.getProductId(), e.getMessage(), e);
                // Allow transaction rollback
                throw new RuntimeException("Failed to confirm reservation for product " + reservation.getProductId(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseReservation(String orderId, UUID eventId) {
        log.info("Attempting to release reservation for orderId: {}, eventId: {}", orderId, eventId);

        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            log.warn("No reservations found for orderId: {} to release. EventId: {}. Assuming already processed or never existed.", orderId, eventId);
            return; // Nothing to release
        }

        for (Reservation reservation : reservations) {
            // Idempotency: Only release PENDING reservations
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                log.warn("Reservation for orderId: {}, productId: {} is not PENDING (status: {}). Skipping release. EventId: {}",
                        orderId, reservation.getProductId(), reservation.getStatus(), eventId);
                continue;
            }

            try {
                Inventory inventory = inventoryRepository.findById(reservation.getProductId())
                        .orElseThrow(() -> {
                            log.error("Inventory record not found for productId {} during release for order {}", reservation.getProductId(), orderId);
                            return new IllegalStateException("Inventory missing for product: " + reservation.getProductId());
                        });

                int previousAvailable = inventory.getAvailableQuantity(); // For audit
                int previousReserved = inventory.getReservedQuantity(); // For audit

                // Update inventory: decrease reserved, increase available
                inventory.releaseReservation(reservation.getQuantity());

                // Save inventory - handle optimistic lock
                inventoryRepository.save(inventory);

                // Update reservation status
                reservation.setStatus(ReservationStatus.RELEASED);
                reservationRepository.save(reservation);

                // Create Audit Log
                StockAuditLog auditLog = new StockAuditLog(
                        inventory.getProductId(), orderId, reservation.getReservationId(),
                        StockChangeType.RESERVATION_RELEASED,
                        reservation.getQuantity(), // Change in available stock
                        previousAvailable, inventory.getAvailableQuantity(),
                        previousReserved, inventory.getReservedQuantity(),
                        "Reservation released for order " + orderId
                );
                stockAuditLogRepository.save(auditLog);

                log.info("Released reservation for orderId: {}, productId: {}", orderId, reservation.getProductId());

            } catch (OptimisticLockingFailureException e) {
                log.error("Optimistic lock failed during release for productId: {}. OrderId: {}. Retrying may be needed.", reservation.getProductId(), orderId, e);
                throw new RuntimeException("Inventory conflict during release, please retry", e);
            } catch (Exception e) {
                log.error("Failed to release reservation for orderId: {}, productId: {}. Error: {}", orderId, reservation.getProductId(), e.getMessage(), e);
                // Allow transaction rollback
                throw new RuntimeException("Failed to release reservation for product " + reservation.getProductId(), e);
            }
        }
    }

    // Optional: Add a scheduled job to handle expired reservations
    // @Scheduled(...)
    // public void cleanupExpiredReservations() { ... }
}