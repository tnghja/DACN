//
//package com.ecommerce.product.service.impl;
//import com.ecommerce.product.config.VNPayConfig;
//import com.ecommerce.product.exception.ApplicationException;
//import com.ecommerce.product.exception.NotFoundException;
//import com.ecommerce.product.exception.ValidationException;
//import com.ecommerce.product.model.dto.PurchaseOrderDTO;
//import com.ecommerce.product.model.entity.*;
//        import com.ecommerce.product.model.request.CheckoutRequest;
//import com.ecommerce.product.model.response.CheckoutResponse;
//import com.ecommerce.product.model.response.PaymentResponse;
//import com.ecommerce.product.repository.*;
//        import com.ecommerce.product.service.CartService;
//import com.ecommerce.product.service.OrderService;
//import com.ecommerce.product.service.PaymentService;
//import com.ecommerce.product.util.VNPayUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.aop.framework.AopContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class OrderServiceImpl implements OrderService {
//    private final ProductRepository productRepository;
//    private final OrderRepository orderRepository;
//    private final PaymentService paymentService;
//    private final VNPayConfig vnPayConfig;
//    private final CartRepository cartRepository;
//    private final CartService cartService;
//    private final OrderItemRepository orderItemRepository;
//    private final CouponRepository couponRepository;
//    private final CartItemRepository cartItemRepository;
//    @Autowired
//    @Lazy  // Thêm annotation này
//    private OrderService orderService;
//    @Override
//    public Order getOrderDetails(Long orderId) {
//        try {
//            return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
//        }  catch (Exception ex) {
//            throw new ApplicationException(ex.getMessage());
//        }
//    }
//
//    @Override
//    public List<Order> getUserOrders(Long userId) {
//        try {
//            return orderRepository.findByUserId(userId);
//        } catch (Exception ex) {
//            throw new ApplicationException(ex.getMessage());
//        }
//    }
//
//    @Override
//    public CheckoutResponse checkoutOrder(CheckoutRequest request) {
//        Cart cart = cartRepository.findById(request.getCartId())
//                .orElseThrow(() -> new ValidationException("Cart doesn't exist for Id: " + request.getCartId()));
//
//        Double totalPrice = cart.getTotal();
//        Double discountPrice = calculateDiscount(cart, request.getDiscountId());
//        Double finalPrice = totalPrice - discountPrice;
//
//        return CheckoutResponse.builder()
//                .totalPrice(totalPrice)
//                .discountPrice(discountPrice)
//                .finalPrice(finalPrice)
//                .build();
//    }
//
//    private Double calculateDiscount(Cart cart, Long couponId) {
//        if (couponId == null) return 0.0;
//
//        Coupon coupon = couponRepository.findById(couponId)
//                .orElseThrow(() -> new ValidationException("Coupon doesn't exist for Id: " + couponId));
//
//        validateCoupon(coupon, cart.getTotal());
//
//        Double discountAmount = applyDiscount(cart.getTotal(), coupon);
//        return (coupon.getMaxDiscountAmount() != null) ? Math.min(discountAmount, coupon.getMaxDiscountAmount()) : discountAmount;
//    }
//
//    private void validateCoupon(Coupon coupon, Double cartTotal) {
//        if (!coupon.getIsActive() || coupon.getStartDate().isAfter(LocalDateTime.now()) || coupon.getEndDate().isBefore(LocalDateTime.now())) {
//            throw new ValidationException("Coupon is not valid or has expired");
//        }
//
//        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
//            throw new ValidationException("Coupon usage limit reached");
//        }
//
//        if (coupon.getMinOrderValue() != null && cartTotal < coupon.getMinOrderValue()) {
//            throw new ValidationException("Order value is less than the minimum required for this coupon");
//        }
//    }
//    private Double applyDiscount(Double totalAmount, Coupon coupon) {
//        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
//            return totalAmount * (coupon.getDiscount() / 100);
//        } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
//            return coupon.getDiscount();
//        }
//        return 0.0;
//    }
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Order placeOrder(Long userId, Long couponId) {
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new ValidationException("Cart doesn't exist for user Id: " + userId));
//
//        if (cart.getCartItems().isEmpty()) {
//            throw new ValidationException("Cart is empty. Cannot place an order.");
//        }
//
//        Double discount = calculateDiscount(cart, couponId);
//        Double finalPrice = cart.getTotal() - discount;
//
//        Order order = copyCartToOrder(cart);
//        order.setTotalPrice(finalPrice);
//        order.setStatus(PaymentStatus.PENDING);
//
//        if (couponId != null) {
//            Coupon coupon = couponRepository.findById(couponId)
//                    .orElseThrow(() -> new ValidationException("Invalid coupon ID: " + couponId));
//            validateCoupon(coupon, cart.getTotal());
//            try {
//                coupon.setUsedCount(coupon.getUsedCount() + 1);
//                couponRepository.save(coupon);  // JPA tự động kiểm tra version ở đây
//                order.setCoupon(coupon);
//                order.setTotalDiscount(discount);
//            } catch (ObjectOptimisticLockingFailureException ex) {
//                // Xử lý khi có xung đột version
//                throw new ApplicationException("Coupon has been modified by another transaction. Please retry.");
//            }
//        }
//
//        return orderRepository.save(order);
//    }
//
//
//
//    public Order copyCartToOrder(Cart cart) {
//        List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
//        if (cartItems.isEmpty()) {
//            throw new NotFoundException("No items in the cart");
//        }
//
//        Order order = new Order();
//        order.setCustomer(cart.getCustomer());
//        order.setOrderDate(LocalDateTime.now());
//        order.setTotalPrice(cart.getTotal());
//        order.setStatus(PaymentStatus.PENDING);
//
//        Order savedOrder = orderRepository.save(order);
//
//        List<OrderItem> orderItemsList = new ArrayList<>();
//        for (CartItem cartItem : cartItems) {
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(savedOrder);
//            orderItem.setProduct(cartItem.getProduct());
//            orderItem.setQuantity(cartItem.getQuantity());
//            orderItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
//            orderItemsList.add(orderItem);
//        }
//
//        orderItemRepository.saveAll(orderItemsList);
//
//        return savedOrder;
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public PaymentResponse.VNPayResponse processingPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, HttpServletRequest request) {
//        Order order = orderService.placeOrder(purchaseOrderDTO.getUserId(), purchaseOrderDTO.getCheckoutRequest().getDiscountId());
//        return paymentService.createVnPayPayment(request, order.getTotalPrice(), "OrderID: " + order.getId());
//    }
//
//    @Override
//    @Transactional(rollbackFor = {Exception.class})
//    public void completeOrder(Map<String, String> reqParams) {
//        validatePayment(reqParams);
//
//        String orderIdStr;
//        try {
//            orderIdStr = reqParams.get("vnp_OrderInfo").split(":")[1].trim();
//        } catch (Exception ex) {
//            throw new ValidationException("Invalid order information in payment response");
//        }
//
//        Long orderId = Long.parseLong(orderIdStr);
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new NotFoundException("Order not found"));
//
//        order.setStatus(PaymentStatus.APPROVED);
//        orderRepository.save(order);
//
//        // Xóa Cart và CartItems sau khi thanh toán thành công
//        Optional<Cart> cart = cartRepository.findByCustomer_Id(order.getCustomer().getId());
//        if (cart.isPresent()) {
//            cartService.deleteAllProductFromCart(order.getCustomer().getId());
//            cartRepository.delete(cart.get());
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = {Exception.class})
//    public void cancelOrder(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new NotFoundException("Order not found"));
//
//        if (!PaymentStatus.PENDING.equals(order.getStatus())) {
//            throw new ValidationException("Cannot cancel a completed order");
//        }
//
//        order.setStatus(PaymentStatus.CANCELLED);
//        orderRepository.save(order);
//
//        // Hoàn lại số lượng sản phẩm
//        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
//        List<Product> productsToUpdate = orderItems.stream()
//                .map(orderItem -> {
//                    Product product = productRepository.findById(orderItem.getProduct().getId())
//                            .orElseThrow(() -> new NotFoundException("Product not found"));
//                    product.setQuantity(product.getQuantity() + orderItem.getQuantity());
//                    return product;
//                })
//                .collect(Collectors.toList());
//        productRepository.saveAll(productsToUpdate);
//
//        // Hoàn lại coupon
//        if (order.getCoupon() != null) {
//            Coupon coupon = couponRepository.findById(order.getCoupon().getId())
//                    .orElseThrow(() -> new NotFoundException("Coupon not found"));
//            if (coupon.getUsedCount() > 0) {
//                try {
//                    coupon.setUsedCount(coupon.getUsedCount() - 1);
//                    couponRepository.save(coupon);  // Kiểm tra version
//                } catch (ObjectOptimisticLockingFailureException ex) {
//                    throw new ApplicationException("Coupon has been modified by another transaction. Please retry.");
//                }
//            }
//        }
//    }
//
//
//    private void validatePayment(Map<String, String> reqParams) {
//        String vnp_SecureHash = reqParams.remove("vnp_SecureHash");
//        if (vnp_SecureHash == null) throw new NotFoundException("vnp_SecureHash is required");
//
//        String hashData = VNPayUtil.getPaymentURL(reqParams, false);
//        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
//        if (!vnpSecureHash.equals(vnp_SecureHash)) throw new ValidationException("Invalid vnp_SecureHash");
//    }
//}
