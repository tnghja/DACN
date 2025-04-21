//package com.ecommerce.product.service.impl;
//
//import java.util.Date;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import com.ecommerce.product.exception.ApplicationException;
//import com.ecommerce.product.exception.NotFoundException;
//import com.ecommerce.product.exception.ValidationException;
//import com.ecommerce.product.model.entity.*;
//import com.ecommerce.product.model.response.CartItemResponse;
//import com.ecommerce.product.model.response.CartResponse;
//import com.ecommerce.product.repository.*;
//import com.ecommerce.product.service.CartService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class CartServiceImpl implements CartService {
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private CartRepository cartRepository;
//    @Autowired
//    private CartItemRepository cartItemRepository;
//    @Autowired
//    private ProductRepository productRepository;
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private OrderItemRepository orderItemRepository;
//    @Override
//    public Cart getCartById(Long userId) {
//        return cartRepository.getCartNotPaidByUser(userId);
//    }
//
////    @Override
////    public CartItems getById(Long cartId, Long courseId) {
////        CartItemsId cartItemsId = new CartItemsId(cartId, courseId);
////        return cartItemRepository.findById(cartItemsId).orElse(null);
////    }
//
//    @Override
//    public Cart createCart(Long userId) {
//        try {
//            // Tìm user theo userId
//            Optional<User> user = userRepository.findById(userId);
//            if (user.isEmpty()) {
//                throw new NotFoundException("User does not exist with ID: " + userId);
//            }
//
//            // Kiểm tra xem user đã có cart hay chưa
//            if (cartRepository.existsByCustomer_Id(userId)) {
//                throw new ValidationException("Cart has already been created for userId " + userId);
//            }
//
//            // Tạo cart mới
//            Cart cart = new Cart();
//            cart.setCustomer(user.get());
//            cartRepository.save(cart);
//
//            return cart;
//        } catch (NotFoundException | ValidationException ex) {
//            throw ex; // Giữ nguyên lỗi gốc
//        } catch (Exception ex) {
//            throw new ApplicationException(ex.getMessage());
//        }
//    }
//
//
//    @Override
//    public void addProductToCart(Long userId, String productId, Integer quantity) {
//        try {
//            Product product = productRepository.findById(productId)
//                    .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
//
//            if (product.getQuantity() < quantity) {
//                throw new ValidationException("Not enough stock for product ID: " + productId);
//            }
//
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
//
//            Cart cart = cartRepository.findByCustomer_Id(userId)
//                    .orElseGet(() -> {
//                        Cart newCart = new Cart();
//                        newCart.setCustomer(user);
//                        newCart.setTotal(0.0);
//                        return cartRepository.save(newCart);
//                    });
//
//            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
//                    .orElse(null);
//
//            if (cartItem != null) {
//                int newQuantity = cartItem.getQuantity() + quantity;
//                if (newQuantity > product.getQuantity()) {
//                    throw new ValidationException("Not enough stock for product ID: " + productId);
//                }
//                cartItem.setQuantity(newQuantity);
//            } else {
//                cartItem = new CartItem();
//                cartItem.setCart(cart);
//                cartItem.setProduct(product);
//                cartItem.setQuantity(quantity);
//            }
//
//            cartItemRepository.save(cartItem);
//            cart.setTotal(cart.getTotal() + (product.getPrice() * quantity));
//            cartRepository.save(cart);
//
//        } catch (NotFoundException | ValidationException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw new ApplicationException(ex.getMessage());
//        }
//    }
//
//
//
////    public CartResponse getById(Long cartId) {
////        Cart cart = cartRepository.findById(cartId).orElse(null);
////        CartResponse cartResponse = new CartResponse(cart.getCartId(), cart.getStudent());
////
////        return cartResponse;
////    }
//
//    @Override
//    public CartResponse getCartByUserId(Long userId) {
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new NotFoundException("Cart not found"));
//
//        // Chuyển danh sách CartItem thành danh sách CartItemResponse
//        List<CartItemResponse> cartItems = cart.getCartItems().stream()
//                .map(CartItemResponse::fromEntity)
//                .toList();
//
//        return new CartResponse(
//                cart.getCartId(),
//                cart.getCustomer().getId(),
//                cart.getTotal(),
//                cartItems
//        );
//    }
//
////    @Transactional
////    public Order copyCartToOrder(Long userId) {
////        try {
////            // Find the cart for the user
////            Cart cart = cartRepository.findByCustomer_Id(userId)
////                    .orElseThrow(() -> new NotFoundException("Cart not found for user ID: " + userId));
////
////            // Get all cart items
////            List<CartItem> cartItems = cartItemRepository.findAllByCart(cart);
////            if (cartItems.isEmpty()) {
////                throw new NotFoundException("No items in the cart");
////            }
////
////            // Create a new Order
////            Order order = new Order();
////            order.setCustomer(cart.getCustomer());
////            order.setOrderDate(new Date());
////            order.setTotalPrice(cart.getTotal());
//////            order.setStatus(OrderStatus.PENDING); // Order status: PENDING
////
////            Order savedOrder = orderRepository.save(order);
////
////            // Convert CartItems to OrderItems
////            List<OrderItem> orderItemsList = new ArrayList<>();
////
////            for (CartItem cartItem : cartItems) {
////                OrderItem orderItem = new OrderItem();
////                orderItem.setOrder(savedOrder);
////                orderItem.setProduct(cartItem.getProduct());
////                orderItem.setQuantity(cartItem.getQuantity());
////                orderItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity()); // Store purchase price
////
////                orderItemsList.add(orderItem);
////            }
////
////            // Save all order items
////            orderItemRepository.saveAll(orderItemsList);
////
////            // Clear the cart after order placement
////            cartItemRepository.deleteAll(cartItems);
////            cartRepository.delete(cart);
////
////            return order;
////        } catch (Exception ex) {
////            throw new ApplicationException("An error occurred while copying the cart to the order: " + ex.getMessage());
////        }
////    }
//
//
////    @Override
////    public Long getIdCartFromStudent(Long studentId) {
////        Student student = studentRepository.findById(studentId).orElse(null);
////        if (student == null) {
////            throw new NotFoundException("Student has not existed with id " + studentId);
////        }
////        Cart cart = cartRepository.findCartByStudent_UserId(studentId);
////        if (cart == null) {
////            throw new NotFoundException("Cart not found");
////        }
////        return cart.getCartId();
////    }
//
//    @Override
//    public void deleteProductFromCart(Long userId, String productId) {
//        // Validate product existence
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
//
//        // Validate user existence
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
//
//        // Check if cart exists for the user
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new ValidationException("Cart doesn't exist for userId: " + userId));
//
//        // Find the CartItem to be deleted
//        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
//                .orElseThrow(() -> new NotFoundException("Product not found in cart with ID: " + productId));
//
//        // Remove the item from the cart
//        cartItemRepository.delete(cartItem);
//
//        // Update cart total price after deletion
//        cart.setTotal(cartItemRepository.calculateTotalPrice(cart.getCartId()));
//        cartRepository.save(cart);
//    }
//
//    @Override
//    public void deleteAllProductFromCart(Long userId) {
//            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
//
//            // Check cart is created or not
//            Cart cart = cartRepository.findByCustomer_Id(userId).orElseThrow(() -> new NotFoundException("Cart doesn't exist for userId: " + userId));
//            cart.setTotal(0.0);
//            cartItemRepository.deleteCartItemsById(cart.getCartId());
//    }
//
//    @Override
//    public void deleteCart(Long userId) {
//        // Validate student existence
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + userId));
//
//        // Check if cart exists for the student
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new ValidationException("Cart doesn't exist for studentId: " + userId));
//
//        // Delete all cart items first
//        deleteAllProductFromCart(userId);
//
//        // Delete the cart
//        cartRepository.delete(cart);
//    }
//    public void deleteListProductFromCart(Long userId, List<String> productIds) {
//        // Validate student existence
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + userId));
//
//        // Check if cart exists for the student
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new ValidationException("Cart doesn't exist for studentId: " + userId));
//
//        // Iterate through the courses to delete
//        for (String productId : productIds) {
//            Product product = productRepository.findById(productId)
//                    .orElseThrow(() -> new NotFoundException("Course not found with ID: " + productId));
//
//            // Check if the course exists in the cart
//            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
//                    .orElseThrow(() -> new ValidationException("Course ID " + productId + " not found in cart"));
//
//            // Remove item from cart
//            cartItemRepository.delete(cartItem);
//        }
//
//        // Update total price after removing items
//        cart.setTotal(cartItemRepository.calculateTotalPrice(cart.getCartId()));
//        cartRepository.save(cart);
//    }
//
//    @Transactional
//    public void updateProductQuantityInCart(Long userId, String productId, Integer quantity) {
//        // Kiểm tra sản phẩm có tồn tại không
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
//
//        // Kiểm tra user có tồn tại không
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
//
//        // Kiểm tra user có giỏ hàng hay chưa
//        Cart cart = cartRepository.findByCustomer_Id(userId)
//                .orElseThrow(() -> new NotFoundException("Cart not found for userId: " + userId));
//
//        // Kiểm tra sản phẩm có trong giỏ hàng không
//        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
//                .orElseThrow(() -> new NotFoundException("Product not found in cart with ID: " + productId));
//
//        // Kiểm tra số lượng hợp lệ
//        if (quantity <= 0) {
//            cartItemRepository.delete(cartItem); // Nếu số lượng <= 0, xóa khỏi giỏ hàng
//        } else {
//            cartItem.setQuantity(quantity);
//            cartItemRepository.save(cartItem);
//        }
//
//        // Cập nhật tổng giá trị giỏ hàng
//        cart.setTotal(cartItemRepository.calculateTotalPrice(cart.getCartId()));
//        cartRepository.save(cart);
//    }
//
//}
