
package com.ecommerce.order.service.impl;
import com.ecommerce.order.model.dto.*;
import com.ecommerce.order.config.VNPayConfig;
import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.exception.*;
import com.ecommerce.order.model.entity.*;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.response.CheckoutResponse;
import com.ecommerce.order.model.response.PaymentResponse;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.repository.CouponRepository;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.httpClient.ProductClient;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;
    private final ProductClient productClient;


    private final VNPayConfig vnPayConfig;
    private final CartRepository cartRepository;
    private final PaymentServiceImpl paymentService;

    @Override
    public Order getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public CheckoutResponse checkoutOrder(CheckoutRequest checkoutRequest) {
        Cart cart = cartRepository.findById(checkoutRequest.getCartId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        Double totalPrice = cart.getTotal();
        Double discountPrice = calculateDiscount(cart, checkoutRequest.getDiscountId());
        Double finalPrice = totalPrice - discountPrice;

        return CheckoutResponse.builder()
                .totalPrice(totalPrice)
                .discountPrice(discountPrice)
                .finalPrice(finalPrice)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order placeOrder(String userId, Long couponId) {
        // 1. Get cart
        Cart cart = cartRepository.findByCustomerId(userId).orElseThrow(() -> new NotFoundException("Cart not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        // 2. Validate inventory
        validateInventory(cart.getCartItems());

        // 3. Process coupon
        Coupon coupon = processCoupon(couponId, cart.getTotal());

        // 4. Create order
        Order order = createOrder(userId, cart, coupon);

        // 5. Clear cart
        cartService.deleteAllProductFromCart(userId);

        return order;
    }

    private void validateInventory(List<CartItem> cartItems) {
        List<InventoryRequest> requests = cartItems.stream()
                .map(item -> new InventoryRequest(
                        item.getProductId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        if (!productClient.checkInventory(requests)) {
            throw new InsufficientStockException("Insufficient stock");
        }
    }

    private Coupon processCoupon(Long couponId, Double totalAmount) {
        if (couponId == null) return null;

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ValidationException("Invalid coupon"));

        validateCoupon(coupon, totalAmount);

        try {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            return couponRepository.save(coupon);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ApplicationException("Coupon conflict");
        }
    }

    private void validateCoupon(Coupon coupon, Double cartTotal) {
        if (!coupon.getIsActive() || coupon.getStartDate().isAfter(LocalDateTime.now()) || coupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Coupon is not valid or has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new ValidationException("Coupon usage limit reached");
        }

        if (coupon.getMinOrderValue() != null && cartTotal < coupon.getMinOrderValue()) {
            throw new ValidationException("Order value is less than the minimum required for this coupon");
        }
    }

    private Order createOrder(String userId, Cart cart, Coupon coupon) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(calculateFinalPrice(cart.getTotal(), coupon));
        order.setStatus(PaymentStatus.PENDING);

        if (coupon != null) {
            order.setCoupon(coupon);
            order.setTotalDiscount(calculateDiscount(cart, coupon.getId()));
        }

        Order savedOrder = orderRepository.save(order);
        createOrderItems(savedOrder, cart.getCartItems());
        return savedOrder;
    }
    private Double calculateDiscount(Cart cart, Long couponId) {
        if (couponId == null) return 0.0;

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ValidationException("Coupon doesn't exist for Id: " + couponId));

        validateCoupon(coupon, cart.getTotal());

        Double discountAmount = applyDiscount(cart.getTotal(), coupon);
        return (coupon.getMaxDiscountAmount() != null) ? Math.min(discountAmount, coupon.getMaxDiscountAmount()) : discountAmount;
    }

    private Double applyDiscount(Double totalAmount, Coupon coupon) {
        if (coupon.getDiscountType().equals(DiscountType.PERCENTAGE)) {
            return totalAmount * (coupon.getDiscount() / 100);
        } else if (coupon.getDiscountType().equals(DiscountType.FIXED)) {
            return coupon.getDiscount();
        }
        return 0.0;
    }

    private void createOrderItems(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            ProductDTO product = productClient.getProduct(cartItem.getProductId());
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProductId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(product.getPrice() * cartItem.getQuantity());
            return item;
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse.VNPayResponse processingPurchaseOrder(PurchaseOrderDTO dto, HttpServletRequest request) {
        Order order = placeOrder(dto.getUserId(), dto.getCheckoutRequest().getDiscountId());
        return paymentService.createVnPayPayment(
                request,
                order.getTotalPrice(),
                "OrderID:" + order.getId()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeOrder(Map<String, String> params) {
        validatePayment(params);

        Long orderId = extractOrderId(params);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setStatus(PaymentStatus.APPROVED);
        orderRepository.save(order);

        productClient.confirmInventory(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException("Cannot cancel order");
        }

        order.setStatus(PaymentStatus.CANCELLED);
        orderRepository.save(order);

        // Rollback operations
        productClient.restoreInventory(orderId);
        rollbackCoupon(order);
    }

    private void rollbackCoupon(Order order) {
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            coupon.setUsedCount(coupon.getUsedCount() - 1);
            couponRepository.save(coupon);
        }
    }

    // Các phương thức hỗ trợ
    private Double calculateFinalPrice(Double total, Coupon coupon) {
        return coupon != null ?
                total - coupon.getDiscount() :
                total;
    }

    private Long extractOrderId(Map<String, String> params) {
        try {
            String orderInfo = params.get("vnp_OrderInfo");
            return Long.parseLong(orderInfo.split(":")[1]);
        } catch (Exception e) {
            throw new ValidationException("Invalid order info");
        }
    }

    private void validatePayment(Map<String, String> params) {
        String secureHash = params.remove("vnp_SecureHash");
        String calculatedHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getSecretKey(),
                VNPayUtil.getPaymentURL(params, false)
        );

        if (!calculatedHash.equals(secureHash)) {
            throw new ValidationException("Invalid payment signature");
        }
    }
}