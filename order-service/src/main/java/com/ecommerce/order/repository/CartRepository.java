package com.ecommerce.order.repository;

import com.ecommerce.order.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.customerId = :customerId")
    Cart getCartNotPaidByCustomerId(@Param("customerId") String customerId);

////    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems WHERE c.customer.id = :userId")
//    Optional<Cart> findByCustomer_Id(String userId);

    boolean existsByCustomerId(String customerId);
    Optional<Cart> findByCustomerId(String customerId);


//    Cart getCartByUserId(Long userId);
}
