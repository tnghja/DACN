//package com.ecommerce.order.model.response;
//
//import com.ecommerce.order.model.entity.CartItem;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.Setter;
//
//@AllArgsConstructor
//@Getter
//@Setter
//public class ProductResponse {
//    private String productId;
//    private String name;
//    private Double price;
//    private Integer quantity; // Add quantity here
//
//    // Convert from CartItem instead of Product
//    public static ProductResponse fromEntity(CartItem cartItem) {
//        return new ProductResponse(
//                cartItem.getProductId(),
//                cartItem.getUnitPrice(),
////                cartItem.getProduct().getPrice(),
//                cartItem.getQuantity() // Get quantity from CartItem
//        );
//    }
//}
