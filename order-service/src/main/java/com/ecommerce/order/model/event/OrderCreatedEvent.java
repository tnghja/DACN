package com.ecommerce.order.model.event;

import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderCreatedEvent(
        Long orderId,
        String userId,
        LocalDateTime orderDate,
        Double totalPrice,
//        List<OrderItemDTO> items,
        PaymentStatus status
) implements Event {
    public static OrderCreatedEvent fromEntity(Order order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getTotalPrice(),
//                order.getOrderItems().stream()
//                        .map(item -> new OrderItemDTO(
//                                item.getProductId(),
//                                item.getQuantity(),
//                                item.getPrice()))
//                        .collect(Collectors.toList()),
                order.getStatus()
        );
    }

    @Override
    public String eventType() {
        return "order-created-event";
    }

    public record OrderItemDTO(
            String productId,
            Integer quantity,
            Double price
    ) {}
}
