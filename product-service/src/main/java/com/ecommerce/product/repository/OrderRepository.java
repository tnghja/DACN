//package com.ecommerce.product.repository;
//
//import com.ecommerce.product.model.entity.Order;
//import com.ecommerce.product.model.entity.PaymentStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface OrderRepository extends JpaRepository<Order, Long> {
//    @Query("SELECT o FROM Order o WHERE o.customer.id = :userId")
//    List<Order> findByUserId(@Param("userId") Long userId);
//
//    boolean existsByCustomerIdAndOrderItemsProductIdAndStatus(Long customerId, String productId, PaymentStatus status);
//
//}
//
