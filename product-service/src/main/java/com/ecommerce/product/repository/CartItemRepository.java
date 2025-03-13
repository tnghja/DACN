package com.ecommerce.product.repository;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.CartItem;
import com.ecommerce.product.model.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    @Modifying
    @Transactional
    @Query("delete from CartItem a where a.cart.cartId = :cartId")
    void deleteCartItemsById(@Param("cartId") Long cartId);

    @Query("SELECT SUM(ci.product.price * ci.quantity) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Double calculateTotalPrice(@Param("cartId") Long cartId);

    @EntityGraph(attributePaths = {"product"})
    List<CartItem> findAllByCart(Cart cart);

    @Query("select a from CartItem a where a.cart.cartId = :cartId and a.product.id = :productId")
    Optional<CartItem> findByIdCardAndIdCourse(@Param("cartId") Long cartId, @Param("productId") String productId);
}
