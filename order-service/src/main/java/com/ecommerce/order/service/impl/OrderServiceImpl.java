
package com.ecommerce.order.service.impl;
import com.ecommerce.order.model.dto.*;
import com.ecommerce.order.config.VNPayConfig;
import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.exception.*;
import com.ecommerce.order.model.entity.*;
import com.ecommerce.order.model.event.*;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.response.CheckoutResponse;
import com.ecommerce.order.model.response.PaymentResponse;
import com.ecommerce.order.repository.*;
import com.ecommerce.order.repository.httpClient.ProductClient;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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

    // ... (Keep existing fields: repositories, kafkaTemplate, configs, etc.) ...
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;
    private final ProductRepository productRepository; // Keep if using local products
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final VNPayConfig vnPayConfig;
    private final CartRepository cartRepository;
    private final PaymentServiceImpl paymentService;


    @Value("${app.kafka.topics.inventory-requests}")
    private String inventoryRequestsTopic;

    // REMOVE or comment out @Value for orderEventsTopic if no longer used anywhere
    // @Value("${app.kafka.topics.order-events}")
    // private String orderEventsTopic;

    // ... (Keep getOrderDetails, getUserOrders, checkoutOrder) ...
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

        // 2. Process coupon
        Coupon coupon = null;
        if (couponId != null ){
            coupon = processCoupon(couponId, cart.getTotal());
        }

        // 3. Create order in PENDING status
        Order order = createOrder(userId, cart, coupon); // Order is saved here

        // 4. Create Order Items
        List<OrderItem> savedOrderItems = createOrderItems(order, cart.getCartItems());

        // 5. Publish event to request inventory reservation (KEEP THIS)
        List<InventoryItemRequest> itemsToReserve = savedOrderItems.stream()
                .map(item -> new InventoryItemRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());
        ReserveInventoryRequestEvent reserveRequest = new ReserveInventoryRequestEvent(String.valueOf(order.getId()), itemsToReserve);
        log.info("Publishing ReserveInventoryRequestEvent for orderId: {}", order.getId());
        kafkaTemplate.send(inventoryRequestsTopic, "reserveInventory", reserveRequest);

        // 6. Clear cart
        cartService.deleteAllProductFromCart(userId);

        // 7. REMOVED: Publish OrderCreatedEvent
        // kafkaTemplate.send(orderEventsTopic, "orderCreated", OrderCreatedEvent.fromEntity(order));

        return order; // Return the order in PENDING status
    }

    // ... (Keep processCoupon, validateCoupon, createOrder, calculateDiscount, applyDiscount, createOrderItems) ...
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
        order.setTotalPrice(calculateFinalPrice(cart.getTotal(), coupon)); // Use cart total initially
        order.setStatus(PaymentStatus.PENDING); // CRITICAL: Start as PENDING

        if (coupon != null) {
            order.setCoupon(coupon);
            order.setTotalDiscount(calculateDiscount(cart, coupon.getId()));
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Created preliminary order with ID: {} in status PENDING", savedOrder.getId());
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
    private List<OrderItem> createOrderItems(Order order, List<CartItem> cartItems) {
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProductId());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getUnitPrice());
            return item;
        }).collect(Collectors.toList());
        return orderItemRepository.saveAll(orderItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse.VNPayResponse processingPurchaseOrder(Long orderId, HttpServletRequest request) {
        // ... (Keep existing logic) ...
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return paymentService.createVnPayPayment(
                request,
                order.getTotalPrice(),
                "OrderID:" + order.getId()
        );
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order placeOrder(PurchaseOrderDTO dto){
        // Place the order as usual
        Order order = placeOrder(dto.getUserId(), dto.getCheckoutRequest().getDiscountId());

        // Save order detail
        com.ecommerce.order.model.request.OrderDetailRequest detail = dto.getOrderDetail();
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .firstName(detail.getFirstName())
                .lastName(detail.getLastName())
                .country(detail.getCountry())
                .streetAddress(detail.getStreetAddress())
                .city(detail.getCity())
                .productName(detail.getProductName())
                .subtotal(detail.getSubtotal())
                .shipping(detail.getShipping())
                .vat(detail.getVat())
                .total(detail.getTotal())
                .paymentMethod(detail.getPaymentMethod())
                .build();
        // Save to repository
        orderDetailRepository.save(orderDetail);
        return order;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeOrder(Map<String, String> params) {
        validatePayment(params); // Ensure payment is valid first

        Long orderId = extractOrderId(params);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != PaymentStatus.PENDING) {
            log.warn("Order {} is not in PENDING state, current state: {}. Cannot complete.", orderId, order.getStatus());

        }

        order.setStatus(PaymentStatus.APPROVED);
        orderRepository.save(order);

        // Publish event to confirm inventory reservation (KEEP THIS)
        InventoryReservationConfirmedEvent confirmEvent = new InventoryReservationConfirmedEvent(String.valueOf(order.getId()));
        log.info("Publishing InventoryReservationConfirmedEvent for orderId: {}", order.getId());
        kafkaTemplate.send(inventoryRequestsTopic, "confirmReservation", confirmEvent);

        // REMOVED: Send original order completed event
        // kafkaTemplate.send(orderEventsTopic, "order-completed", new OrderStatusEvent(...));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Allow cancellation only if PENDING (or other appropriate states based on inventory feedback)
        if (order.getStatus() != PaymentStatus.PENDING) {
            throw new ValidationException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(PaymentStatus.CANCELLED);
        orderRepository.save(order);

        // Publish event to release inventory reservation (KEEP THIS)
        InventoryReservationReleasedEvent releaseEvent = new InventoryReservationReleasedEvent(String.valueOf(order.getId()));
        log.info("Publishing InventoryReservationReleasedEvent for orderId: {}", order.getId());
        kafkaTemplate.send(inventoryRequestsTopic, "releaseReservation", releaseEvent);

        // Rollback coupon (remains the same)
        rollbackCoupon(order);

        // REMOVED: Send original order cancelled event
        // kafkaTemplate.send(orderEventsTopic, "order-cancelled", new OrderStatusEvent(...));
    }

    // ... (Keep rollbackCoupon, calculateFinalPrice, extractOrderId, validatePayment) ...
    private void rollbackCoupon(Order order) {
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            couponRepository.findById(coupon.getId()).ifPresent(freshCoupon -> {
                if (freshCoupon.getUsedCount() > 0) {
                    freshCoupon.setUsedCount(freshCoupon.getUsedCount() - 1);
                    couponRepository.save(freshCoupon);
                    log.info("Rolled back coupon usage for code: {} on order {}", freshCoupon.getCouponCode(), order.getId());
                } else {
                    log.warn("Coupon {} for order {} already had 0 used count during rollback attempt.", freshCoupon.getCouponCode(), order.getId());
                }
            });
        }
    }
    private Double calculateFinalPrice(Double total, Coupon coupon) {
        // This calculation might need refinement. The discount application
        // depends on the coupon being valid *at the time of order placement*.
        // Storing the calculated discount amount on the Order itself might be better.
        if (coupon != null) {
            // Re-calculate or fetch stored discount? For now, recalculating:
            Double discountAmount = applyDiscount(total, coupon); // 'total' here is cart total before discount
            Double finalDiscount = (coupon.getMaxDiscountAmount() != null) ? Math.min(discountAmount, coupon.getMaxDiscountAmount()) : discountAmount;
            return total - finalDiscount;
        }
        return total;
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