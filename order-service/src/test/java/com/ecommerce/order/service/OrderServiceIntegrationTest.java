//package com.ecommerce.order.service;
//
//import com.ecommerce.order.model.dto.PurchaseOrderDTO;
//import com.ecommerce.order.model.entity.Coupon;
//import com.ecommerce.order.model.entity.DiscountType;
//import com.ecommerce.order.model.entity.Order;
//import com.ecommerce.order.model.request.CheckoutRequest;
//import com.ecommerce.order.model.response.CheckoutResponse;
//import com.ecommerce.order.model.response.PaymentResponse;
//import com.ecommerce.order.repository.CouponRepository;
//import com.ecommerce.order.repository.OrderItemRepository;
//import com.ecommerce.order.repository.OrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import javax.servlet.http.HttpServletRequest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class OrderServiceIntegrationTest {
//
//    @Autowired
//    private OrderService orderService;
//
//    @Autowired
//    private CouponRepository couponRepository;
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private OrderItemRepository orderItemRepository;
//
//    @BeforeEach
//    void setup() {
//        // Clean up before each test
//        orderItemRepository.deleteAll();
//        orderRepository.deleteAll();
//        couponRepository.deleteAll();
//
//        // Create and persist a valid coupon
//        Coupon coupon = Coupon.builder()
//                .name("Test Coupon")
//                .couponCode("TEST10")
//                .discountType(DiscountType.PERCENTAGE)
//                .discount(10.0)
//                .startDate(LocalDateTime.now().minusDays(1))
//                .endDate(LocalDateTime.now().plusDays(1))
//                .usageLimit(100)
//                .usedCount(0)
//                .maxDiscountAmount(50.0)
//                .minOrderValue(20.0)
//                .isActive(true)
//                .build();
//        couponRepository.save(coupon);
//        // Optionally, create and persist an order and order item if needed for tests
//    }
//
//    @Test
//    void checkoutOrder_validRequest() {
//        CheckoutRequest request = new CheckoutRequest();
//        request.setCartId(1L);
//        request.setDiscountId(1L);
//        CheckoutResponse response = orderService.checkoutOrder(request);
//        assertNotNull(response);
//    }
//
//    @Test
//    void checkoutOrder_invalidRequest() {
//        CheckoutRequest request = new CheckoutRequest();
//        assertThrows(Exception.class, () -> orderService.checkoutOrder(request));
//    }
//
//    @Test
//    void placeOrder_validDTO() {
//        PurchaseOrderDTO dto = new PurchaseOrderDTO();
//        dto.setPrices(CheckoutResponse.builder()
//                .totalPrice(100.0)
//                .discountPrice(10.0)
//                .finalPrice(90.0)
//                .build());
//        Order order = orderService.placeOrder(dto);
//        assertNotNull(order);
//    }
//
//    @Test
//    void placeOrder_invalidDTO() {
//        PurchaseOrderDTO dto = new PurchaseOrderDTO();
//        assertThrows(Exception.class, () -> orderService.placeOrder(dto));
//    }
//
//    @Test
//    void placeOrder_withUserAndCoupon_valid() {
//        Order order = orderService.placeOrder("user1", 1L);
//        assertNotNull(order);
//    }
//
//    @Test
//    void placeOrder_withUserAndCoupon_invalidCoupon() {
//        assertThrows(Exception.class, () -> orderService.placeOrder("user1", 99999L));
//    }
//
//    @Test
//    void processingPurchaseOrder_success() {
//        // This test assumes a valid order exists and HttpServletRequest can be mocked
//        // HttpServletRequest request = mock(HttpServletRequest.class);
//        // PaymentResponse.VNPayResponse response = orderService.processingPurchaseOrder(1L, request);
//        // assertNotNull(response);
//        assertTrue(true, "Payment processing test goes here with mocks.");
//    }
//
//    @Test
//    void processingPurchaseOrder_failure() {
//        // HttpServletRequest request = mock(HttpServletRequest.class);
//        // assertThrows(Exception.class, () -> orderService.processingPurchaseOrder(99999L, request));
//        assertTrue(true, "Payment failure test goes here with mocks.");
//    }
//
//    @Test
//    void completeOrder_validParams() {
//        Map<String, String> params = Map.of("orderId", "1", "status", "COMPLETED");
//        assertDoesNotThrow(() -> orderService.completeOrder(params));
//    }
//
//    @Test
//    void cancelOrder_existingOrder() {
//        assertDoesNotThrow(() -> orderService.cancelOrder(1L));
//    }
//
//    @Test
//    void cancelOrder_nonExistentOrder() {
//        assertDoesNotThrow(() -> orderService.cancelOrder(99999L));
//    }
//
//    @Test
//    void getOrderDetails_existingOrder() {
//        Order order = orderService.getOrderDetails(1L);
//        assertNotNull(order);
//    }
//
//    @Test
//    void getOrderDetails_nonExistentOrder() {
//        Order order = orderService.getOrderDetails(99999L);
//        assertNull(order);
//    }
//
//    @Test
//    void getUserOrders_existingUser() {
//        List<Order> orders = orderService.getUserOrders("user1");
//        assertNotNull(orders);
//    }
//
//    @Test
//    void getUserOrders_noOrders() {
//        List<Order> orders = orderService.getUserOrders("nouser");
//        assertNotNull(orders);
//        assertTrue(orders.isEmpty() || orders.size() == 0);
//    }
//}
//
