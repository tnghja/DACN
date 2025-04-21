package com.ecommerce.order.model.event;

import com.ecommerce.order.model.entity.PaymentStatus;

import java.time.LocalDateTime;

public record OrderStatusEvent(
        Long orderId,
        String userId,
        PaymentStatus newStatus,
        LocalDateTime updateTime,
        String reason
) implements Event {
    @Override
    public String eventType() {
        return "order-status-event";
    }
}