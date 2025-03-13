package com.ecommerce.product.service.impl;

import com.ecommerce.product.config.VNPayConfig;
import com.ecommerce.product.exception.ApplicationException;
import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.exception.ValidationException;
import com.ecommerce.product.model.dto.PurchaseOrderDTO;
import com.ecommerce.product.model.entity.*;
import com.ecommerce.product.model.response.CheckoutResponse;
import com.ecommerce.product.model.response.PaymentResponse;
import com.ecommerce.product.repository.*;
import com.ecommerce.product.service.CartService;
import com.ecommerce.product.service.OrderService;
import com.ecommerce.product.service.PaymentService;
import com.ecommerce.product.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final VNPayConfig vnPayConfig;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public CheckoutResponse checkoutOrder(Long userId) {
        try {
            Cart cart = cartRepository.findByCustomer_Id(userId).orElseThrow(
                    () -> new ValidationException("Cart doesn't exist for user Id: " + userId)
            );
            Double totalPrice = cart.getTotal();
            return CheckoutResponse.builder()
                    .totalPrice(totalPrice)
                    .build();
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public Order placeOrder(Long userId) {
        try {
            Cart cart = cartRepository.findByCustomer_Id(userId).orElseThrow(
                    () -> new ValidationException("Cart doesn't exist for user Id: " + userId)
            );

            // Kiểm tra số lượng tồn kho
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = productRepository.findById(cartItem.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Product not found"));

                if (product.getQuantity() < cartItem.getQuantity()) {
                    throw new ValidationException("Product " + product.getName() + " is out of stock.");
                }
            }

            // Tạo đơn hàng nếu tất cả sản phẩm đều có đủ hàng
            Order order = Order.builder()
                    .customer(cart.getCustomer())
                    .totalPrice(cart.getTotal())
                    .status(PaymentStatus.PENDING)
                    .build();
            order = orderRepository.save(order);

            // Giảm số lượng tồn kho và chuyển sản phẩm từ giỏ hàng sang đơn hàng
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = productRepository.findById(cartItem.getProduct().getId()).get();
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .build();
                orderItemRepository.save(orderItem);
            }

            return order;
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }


    @Override
    public PaymentResponse.VNPayResponse processingPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, HttpServletRequest request) {
        try {
            Order order = placeOrder(purchaseOrderDTO.getUserId());
            return paymentService.createVnPayPayment(request, order.getTotalPrice(), "OrderID: " + order.getId());
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void completeOrder(Map<String, String> reqParams) {
        try {
            validatePayment(reqParams);

            String orderIdStr = reqParams.get("vnp_OrderInfo").split(":")[1].trim();
            Long orderId = Long.parseLong(orderIdStr);

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found"));

            // Cập nhật trạng thái thanh toán
            order.setStatus(PaymentStatus.APPROVED);
            orderRepository.save(order);

            // Lấy danh sách OrderItem liên quan đến Order
            List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

            // Trừ số lượng tồn kho
            for (OrderItem orderItem : orderItems) {
                Product product = orderItem.getProduct();
                int newQuantity = product.getQuantity() - orderItem.getQuantity();

                if (newQuantity < 0) {
                    throw new ValidationException("Product " + product.getName() + " is out of stock");
                }

                product.setQuantity(newQuantity);
                productRepository.save(product); // Cập nhật số lượng sản phẩm
            }

            // Xóa giỏ hàng sau khi thanh toán thành công
            cartService.deleteAllProductFromCart(order.getCustomer().getId());

        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }


    @Override
    public void cancelOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

            if (!PaymentStatus.PENDING.equals(order.getStatus())) {
                throw new ValidationException("Cannot cancel a completed order");
            }

            order.setStatus(PaymentStatus.CANCELLED);
            orderRepository.save(order);

            // Hoàn lại số lượng sản phẩm vào kho
            List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);
            for (OrderItem orderItem : orderItems) {
                Product product = productRepository.findById(orderItem.getProduct().getId()).get();
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }

        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }


    @Override
    public Order getOrderDetails(Long orderId) {
        try {
            return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        }  catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        try {
            return orderRepository.findByUserId(userId);
        } catch (Exception ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    private void validatePayment(Map<String, String> reqParams) {
        try {
            String vnp_SecureHash = reqParams.remove("vnp_SecureHash");
            if (vnp_SecureHash == null) throw new NotFoundException("vnp_SecureHash is required");
            String hashData = VNPayUtil.getPaymentURL(reqParams, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
            if (!vnpSecureHash.equals(vnp_SecureHash)) throw new ValidationException("Invalid vnp_SecureHash");
        } catch (Exception e) {
            log.error("Payment validation error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
