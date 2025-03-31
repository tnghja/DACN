package com.ecommerce.order.repository;

import com.ecommerce.order.model.entity.Cart;
import com.ecommerce.order.model.entity.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, String productId);
    @Modifying
    @Transactional
    @Query("delete from CartItem a where a.cart.cartId = :cartId")
    void deleteCartItemsById(@Param("cartId") Long cartId);

    @Query("SELECT SUM(ci.unitPrice * ci.quantity) FROM CartItem ci WHERE ci.cart = :cart")
    Double calculateCartTotal(@Param("cart") Cart cart);

    @EntityGraph(attributePaths = {"product"})
    List<CartItem> findAllByCart(Cart cart);

    void deleteByCart(Cart cart);

//    @Query("select a from CartItem a where a.cart.cartId = :cartId and a.product.id = :productId")
//    Optional<CartItem> findByIdCardAndIdCourse(@Param("cartId") Long cartId, @Param("productId") String productId);
}
