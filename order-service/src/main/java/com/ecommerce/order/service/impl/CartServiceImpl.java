package com.ecommerce.order.service.impl;

import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.exception.ValidationException;
import com.ecommerce.order.model.dto.CustomerDTO;
import com.ecommerce.order.model.dto.ProductDTO;
import com.ecommerce.order.model.entity.Cart;
import com.ecommerce.order.model.entity.CartItem;
import com.ecommerce.order.model.entity.Product;
import com.ecommerce.order.model.response.CartItemResponse;
import com.ecommerce.order.model.response.CartResponse;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.repository.ProductRepository;
import com.ecommerce.order.repository.httpClient.ProductClient;
import com.ecommerce.order.repository.httpClient.UserClient;
import com.ecommerce.order.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

//    private final ProductClient productClient;

    private final UserClient userClient;
//    @Autowired
    private final ProductRepository productRepository;

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
//        CustomerDTO user = userClient.getCustomer(customerId)
//                .orElseThrow(() -> new NotFoundException("User not found with ID: " + customerId));

        if (cartRepository.existsByCustomerId(customerId)) {
            throw new ValidationException("Cart already exists for user: " + customerId);
        }

        Cart newCart = Cart.builder()
                .customerId(customerId)
                .total(0.0)
                .build();

        return cartRepository.save(newCart);
    }


    @Override
    public void addProductToCart(String userId, String productId, Integer quantity) {
        // 1. Get product details from LOCAL database
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found locally with ID: " + productId)); // Use local ProductNotFoundException


        // 2. Process cart logic (remains mostly the same)
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseGet(() -> createCart(userId)); // createCart still needs UserClient check

        // Use price from the local Product entity
        processCartItem(cart, productId, quantity, product.getPrice());
    }

    private void processCartItem(Cart cart, String productId, Integer quantity, Double price) {
        CartItem existingItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .quantity(quantity)
                    .unitPrice(price)
                    .build();
            cartItemRepository.save(newItem);
            if (cart.getCartItems() == null) {
                cart.setCartItems(new ArrayList<>());
            }
            cart.getCartItems().add(newItem);
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

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return CartResponse.builder()
                    .cartId(cart.getCartId())
                    .userId(cart.getCustomerId())
                    .total(0.0)
                    .products(Collections.emptyList())
                    .build();
        }

        // Get list of product IDs in the cart
        List<String> productIds = cart.getCartItems().stream()
                .map(CartItem::getProductId)
                .distinct() // Avoid duplicate lookups if item added multiple times (though your model might prevent this)
                .collect(Collectors.toList());

        // Fetch product details LOCALLY for all products in the cart
        List<Product> products = productRepository.findAllById(productIds); // Efficiently fetches multiple products

        // Build the response using local data
        return buildCartResponse(cart, products);
    }
    // Modify buildCartResponse to use local Product entities
    private CartResponse buildCartResponse(Cart cart, List<Product> products) {
        // Create a map for easy lookup
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> convertToItemResponse(item, productMap))
                .collect(Collectors.toList());

        // Recalculate total based on cart item prices (which were set when added)
        // or ensure cart.getTotal() was updated correctly.
        // Double calculatedTotal = calculateTotal(items); // Or use cart.getTotal() if reliable
        cart.setTotal(calculateTotal(items)); // Ensure cart total is accurate
        cartRepository.save(cart); // Persist updated total


        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getCustomerId())
                .total(cart.getTotal())
                .products(items)
                .build();
    }
    // Modify convertToItemResponse to use local Product entity
    private CartItemResponse convertToItemResponse(CartItem item, Map<String, Product> productMap) {
        // Get the local product data, handle case where it might be missing (though shouldn't happen ideally)
        Product product = productMap.get(item.getProductId());
        String productName = (product != null) ? product.getName() : "Product Name Unavailable";
         String productCover = (product != null) ? product.getCover() : null;
         String productBrand = (product != null) ? product.getBrand() : null;
         Boolean isAvailable = product != null && product.getDeleteAt() == null; // Check if product is active

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(productName) // Use name from local product
                .price(item.getUnitPrice()) // Use price stored at time of adding to cart
                .quantity(item.getQuantity())
                // .imageUrl(productCover) // Add other fields if needed
                 .brand(productBrand)
                 .isAvailable(isAvailable)
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
        if (cart.getCartItems() != null) {
            cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
        }
        // 5. Update total
        updateCartTotal(cart);
    }

    @Override
    @Transactional
    public void deleteAllProductFromCart(String userId) {
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }
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

    @Override
    @Transactional
    // Remove @CircuitBreaker
    public void updateProductQuantityInCart(String userId, String productId, Integer quantity) {
        // 1. Get product from LOCAL database (just to ensure it exists conceptually)
        // We don't actually need its details like price here, as unitPrice is in CartItem
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found locally with ID: " + productId));

        // 2. Get cart
        Cart cart = cartRepository.findByCustomerId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        // 3. Find item
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new NotFoundException("Product not in cart"));

        // 4. REMOVED Stock Check: Inventory service handles this.
        // if(quantity > product.getQuantity()) {
        //     throw new ValidationException("Exceeds available stock");
        // }

        // 5. Update quantity (logic remains same)
        if(quantity <= 0) {
            cartItemRepository.delete(cartItem);
            if (cart.getCartItems() != null) {
                cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
            }
        } else {
            // NOTE: unitPrice is NOT updated here. It keeps the price from when it was first added.
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        // 6. Update cart total
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
