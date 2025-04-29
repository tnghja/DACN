package com.ecommerce.order.repository;

import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
   @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    List<Order> findByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndOrderItemsProductIdAndStatus(String userId, String productId, PaymentStatus status);

}

