package com.ecommerce.product.repository;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.CartItem;
import com.ecommerce.product.model.entity.Order;
import com.ecommerce.product.model.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<OrderItem> findAllByOrder(Order order);

}
