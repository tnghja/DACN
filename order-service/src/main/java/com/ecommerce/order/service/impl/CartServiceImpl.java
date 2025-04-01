package com.ecommerce.order.service.impl;

import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.exception.ValidationException;
import com.ecommerce.order.model.dto.CustomerDTO;
import com.ecommerce.order.model.dto.ProductDTO;
import com.ecommerce.order.model.entity.Cart;
import com.ecommerce.order.model.entity.CartItem;
import com.ecommerce.order.model.response.CartItemResponse;
import com.ecommerce.order.model.response.CartResponse;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.repository.httpClient.ProductClient;
import com.ecommerce.order.repository.httpClient.UserClient;
import com.ecommerce.order.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
//    @Autowired
//    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private UserClient userClient;
//    @Autowired
//    private ProductRepository productRepository;
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private OrderItemRepository orderItemRepository;
    @Override
    public Cart getCartById(String userId) {
        return cartRepository.getCartNotPaidByCustomerId(userId);
    }

//    @Override
//    public CartItems getById(Long cartId, Long courseId) {
//        CartItemsId cartItemsId = new CartItemsId(cartId, courseId);
//        return cartItemRepository.findById(cartItemsId).orElse(null);
//    }

    @Override
    public Cart createCart(String customerId) {
        // Gọi User Service để validate user
        CustomerDTO user = userClient.getCustomer(customerId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + customerId));

        if (cartRepository.existsByCustomerId(customerId)) {
            throw new ValidationException("Cart already exists for user: " + customerId);
        }

        Cart newCart = Cart.builder()
                .customerId(customerId)
                .total(0.0)
                .build();

        return cartRepository.save(newCart);
    }


    @CircuitBreaker(name = "productService", fallbackMethod = "addProductFallback")
    @Override
    public void addProductToCart(String userId, String productId, Integer quantity) {
        // 1. Kiểm tra product tồn tại qua Product Service
        ProductDTO product = productClient.getProduct(productId);

        // 2. Kiểm tra số lượng tồn kho
        if (product.getQuantity() < quantity) {
            throw new ValidationException("Not enough stock for product: " + productId);
        }

        // 3. Xử lý logic giỏ hàng
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseGet(() -> createCart(userId));

        processCartItem(cart, productId, quantity, product.getPrice());
    }

    private void processCartItem(Cart cart, String productId, Integer quantity, Double price) {
        CartItem existingItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .quantity(quantity)
                    .unitPrice(price)
                    .build();
            cartItemRepository.save(newItem);
        }

        updateCartTotal(cart);
    }
    private void addProductFallback(Long userId, String productId, Integer quantity, Throwable t) throws ServiceUnavailableException {
        // Logic dự phòng: log cảnh báo, lưu vào queue retry...
        throw new ServiceUnavailableException("Product service is temporarily unavailable");
    }

//    public CartResponse getById(Long cartId) {
//        Cart cart = cartRepository.findById(cartId).orElse(null);
//        CartResponse cartResponse = new CartResponse(cart.getCartId(), cart.getStudent());
//
//        return cartResponse;
//    }
    private void updateCartTotal(Cart cart) {
        Double total = cartItemRepository.calculateCartTotal(cart);
        cart.setTotal(total != null ? total : 0.0);
        cartRepository.save(cart);
    }
    @Override
    public CartResponse getCartByUserId(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + customerId));

        // Lấy danh sách product IDs trong giỏ
        List<String> productIds = cart.getCartItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        // Gọi batch API
        List<ProductDTO> products = productClient.getProductsBatch(productIds);

        return buildCartResponse(cart, products);
    }
    private CartResponse buildCartResponse(Cart cart, List<ProductDTO> products) {
        Map<String, ProductDTO> productMap = products.stream()
                .collect(Collectors.toMap(ProductDTO::getId, Function.identity()));

        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> convertToItemResponse(item, productMap))
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getCustomerId())
                .total(calculateTotal(items))
                .products(items)
                .build();
    }
    private CartItemResponse convertToItemResponse(CartItem item, Map<String, ProductDTO> productMap) {
        ProductDTO product = productMap.getOrDefault(item.getProductId(),
                ProductDTO.builder().id(item.getProductId()).build());

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(product.getName())
                .price(item.getUnitPrice())
                .quantity(item.getQuantity())
//                .total(item.getUnitPrice() * item.getQuantity())
//                .imageUrl(product.getCover())
                .build();
    }
//    @Transactional
//    public Order copyCartToOrder(Long userId) {
//        try {
//            // Find the cart for the user
//            Cart cart = cartRepository.findByCustomer_Id(userId)
//                    .orElseThrow(() -> new NotFoundException("Cart not found for user ID: " + userId));
//
//            // Get all cart items
//            List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
//            if (cartItems.isEmpty()) {
//                throw new NotFoundException("No items in the cart");
//            }
//
//            // Create a new Order
//            Order order = new Order();
//            order.setCustomer(cart.getCustomer());
//            order.setOrderDate(new Date());
//            order.setTotalPrice(cart.getTotal());
////            order.setStatus(OrderStatus.PENDING); // Order status: PENDING
//
//            Order savedOrder = orderRepository.save(order);
//
//            // Convert CartItems to OrderItems
//            List<OrderItem> orderItemsList = new ArrayList<>();
//
//            for (CartItem cartItem : cartItems) {
//                OrderItem orderItem = new OrderItem();
//                orderItem.setOrder(savedOrder);
//                orderItem.setProduct(cartItem.getProduct());
//                orderItem.setQuantity(cartItem.getQuantity());
//                orderItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity()); // Store purchase price
//
//                orderItemsList.add(orderItem);
//            }
//
//            // Save all order items
//            orderItemRepository.saveAll(orderItemsList);
//
//            // Clear the cart after order placement
//            cartItemRepository.deleteAll(cartItems);
//            cartRepository.delete(cart);
//
//            return order;
//        } catch (Exception ex) {
//            throw new ApplicationException("An error occurred while copying the cart to the order: " + ex.getMessage());
//        }
//    }



    @Override
    public void deleteProductFromCart(String userId, String productId) {
        // 1. Validate user
        userClient.getCustomer(userId);

        // 2. Get cart
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        // 3. Find cart item
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new NotFoundException("Product not in cart"));

        // 4. Remove item
        cartItemRepository.delete(cartItem);

        // 5. Update total
        updateCartTotal(cart);
    }

    @Override
    @Transactional
    public void deleteAllProductFromCart(String userId) {
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        cart.setTotal(0.0);
        cartRepository.save(cart);
    }

    @Override
    public void deleteCart(String userId) {
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        deleteAllProductFromCart(userId);
        cartRepository.delete(cart);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "deleteProductsFallback")
    @Override
    @Transactional
    public void deleteListProductFromCart(String userId, List<String> productIds) {
        // 1. Validate user
        userClient.getCustomer(userId);

        // 2. Get cart
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        // 3. Delete items
        productIds.forEach(productId -> {
            CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                    .orElseThrow(() -> new NotFoundException("Product " + productId + " not in cart"));
            cartItemRepository.delete(cartItem);
        });

        // 4. Update total
        updateCartTotal(cart);
    }

    // Fallback method
    private void deleteProductsFallback(String userId, List<String> productIds, Throwable t) throws ServiceUnavailableException {
        throw new ServiceUnavailableException("Product service unavailable");
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "updateQuantityFallback")
    @Transactional
    public void updateProductQuantityInCart(String userId, String productId, Integer quantity) {
        // 1. Validate product existence
        ProductDTO product = productClient.getProduct(productId);

        // 2. Get cart
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        // 3. Find item
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new NotFoundException("Product not in cart"));

        // 4. Validate stock
        if(quantity > product.getQuantity()) {
            throw new ValidationException("Exceeds available stock");
        }

        // 5. Update quantity
        if(quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        updateCartTotal(cart);
    }

    private void updateQuantityFallback(String userId, String productId, Integer quantity, Throwable t) throws ServiceUnavailableException {
//        logger.error("Failed to update quantity: {}", t.getMessage());
        throw new ServiceUnavailableException("Product service unavailable");
    }

    private Double calculateTotal(List<CartItemResponse> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
