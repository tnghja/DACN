package com.ecommerce.order.service;



import com.ecommerce.order.model.entity.Coupon;
import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.entity.PaymentStatus;
import com.ecommerce.order.model.event.*;
import com.ecommerce.order.repository.CouponRepository;
import com.ecommerce.order.repository.OrderRepository; // Inject repository
import lombok.RequiredArgsConstructor; // Use constructor injection
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler; // Import
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
// No @Payload needed for specific handlers
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For DB updates

import java.util.Optional;



@Service
@Slf4j
@RequiredArgsConstructor
// FIX: Listen ONLY to the order-feedback topic
@KafkaListener(topics = {"${app.kafka.topics.order-feedback}"},
        groupId = "order-feedback-group", // Specific group ID for feedback
        containerFactory = "orderListenerContainerFactory")
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;

    // --- REMOVED Handlers for internal Order Events ---
    /*
    @KafkaHandler
    public void handleOrderCreated(OrderCreatedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        // Removed...
    }

    @KafkaHandler
    public void handleOrderStatusUpdate(OrderStatusEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
       // Removed internal status handling based on key...
    }
    */

    // --- Keep Handlers for Inventory Feedback Events ---

    @KafkaHandler
    @Transactional
    public void handleInventoryReserved(InventoryReservationConfirmedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Handling InventoryReservedEvent [Key: {}]: {}", key, event);
        // ... (Keep existing logic from previous step) ...
        try {
            Long orderId = Long.parseLong(event.orderId());
            Optional<Order> orderOpt = orderRepository.findById(orderId);

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if (order.getStatus() == PaymentStatus.PENDING) {
                    log.info("Inventory reserved for Order ID: {}. Status remains PENDING.", orderId);
                    // Potentially update status here if needed, e.g., order.setStatus(PaymentStatus.WAITING_PAYMENT);
                    // orderRepository.save(order);
                } else {
                    log.warn("Received InventoryReservedEvent for Order ID: {} which is not in PENDING state (state: {}). Ignoring.", orderId, order.getStatus());
                }
            } else {
                log.warn("Received InventoryReservedEvent for non-existent Order ID: {}", orderId);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid orderId format received in InventoryReservedEvent: {}", event.orderId(), e);
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent for orderId {}: {}", event.orderId(), e.getMessage(), e);
            // throw new RuntimeException("Failed processing InventoryReservedEvent for order " + event.orderId(), e);
        }
    }

    @KafkaHandler
    @Transactional
    public void handleInventoryFailed(InventoryReservationFailedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Handling InventoryReservationFailedEvent [Key: {}]: {}", key, event);
        // ... (Keep existing logic from previous step) ...
        try {
            Long orderId = Long.parseLong(event.orderId());
            Optional<Order> orderOpt = orderRepository.findById(orderId);

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if (order.getStatus() == PaymentStatus.PENDING) {
                    log.warn("Inventory reservation failed for Order ID: {}. Reason: {}. Cancelling order.", orderId, event.reason());
                    order.setStatus(PaymentStatus.CANCELLED); // Or FAILED_NO_STOCK
                    orderRepository.save(order);
                    rollbackCoupon(order); // Use helper method
                } else {
                    log.warn("Received InventoryReservationFailedEvent for Order ID: {} which is not in PENDING state (state: {}). Ignoring cancellation.", orderId, order.getStatus());
                }
            } else {
                log.warn("Received InventoryReservationFailedEvent for non-existent Order ID: {}", orderId);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid orderId format received in InventoryReservationFailedEvent: {}", event.orderId(), e);
        } catch (Exception e) {
            log.error("Error processing InventoryReservationFailedEvent for orderId {}: {}", event.orderId(), e.getMessage(), e);
            // throw new RuntimeException("Failed processing InventoryReservationFailedEvent for order " + event.orderId(), e);
        }
    }

    // Keep default handler
    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object payload, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.warn("Received unhandled event type or message on feedback listener [Key: {}]: Type: {}", key,
                (payload != null ? payload.getClass().getName() : "null"));
    }

    // Keep helper method
    private void rollbackCoupon(Order order) {
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            couponRepository.findById(coupon.getId()).ifPresent(freshCoupon -> {
                if (freshCoupon.getUsedCount() > 0) {
                    freshCoupon.setUsedCount(freshCoupon.getUsedCount() - 1);
                    couponRepository.save(freshCoupon);
                    log.info("Rolled back coupon usage for code: {} on order {}", freshCoupon.getCouponCode(), order.getId());
                } else {
                    log.warn("Coupon {} for order {} already had 0 used count during rollback attempt.", freshCoupon.getCouponCode(), order.getId());
                }
            });
        }
    }
}