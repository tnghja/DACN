package com.ecommerce.product.repository;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.CartItem;
import com.ecommerce.product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.customer.id = :userId")
    Cart getCartNotPaidByUser(@Param("userId") Long userId);


    boolean existsByCustomer_Id(Long userId);
    Optional<Cart> findByCustomer_Id(Long userId);


//    Cart getCartByUserId(Long userId);
}
