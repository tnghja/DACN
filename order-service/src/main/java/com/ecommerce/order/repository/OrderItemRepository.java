package com.ecommerce.order.repository;

import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<OrderItem> findAllByOrder(Order order);

    List<OrderItem> findByOrderId(Long orderId);
}
