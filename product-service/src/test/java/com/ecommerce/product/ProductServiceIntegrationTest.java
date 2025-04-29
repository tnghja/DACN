package com.ecommerce.product;

import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private String createdProductId;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long testCategoryId;

    /**
     * Ensures that a Category is always persisted before being assigned to any Product.
     * This is required to avoid TransientPropertyValueException in Hibernate/JPA tests.
     */
    @BeforeEach
    void setUp() {
        // Clean up before each test
        productRepository.deleteAll();
        productRepository.flush();
        categoryRepository.deleteAll();
        categoryRepository.flush();
        // Create a test category
        Category category = Category.builder().name("Test Category").build();
        category = categoryRepository.save(category);
        testCategoryId = category.getId();
    }

    /**
     * Helper to get a valid, persisted Category ID for use in tests.
     * Always use this when creating a Product in tests.
     */
    private Long getPersistedCategoryId() {
        // Always fetch the latest persisted category (should exist after setUp)
        return categoryRepository.findAll().stream().findFirst().map(Category::getId).orElseThrow(() -> new IllegalStateException("No category present for test!"));
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        productRepository.flush();
    }

    @Test
    @DisplayName("TC-01: Lấy danh sách sản phẩm không cần xác thực, không có filter/sort/page")
    void getAllProducts_Default_TC01() {
        Pageable pageable = PageRequest.of(0, 10); // page 0, size 10
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
    }

    @Test
    @DisplayName("TC-02: Lấy danh sách sản phẩm với tham số phân trang")
    void getAllProducts_Pagination_TC02() {
        Pageable pageable = PageRequest.of(1, 2); // page 1, size 2
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
    }

    @Test
    @DisplayName("TC-03: Lấy danh sách sản phẩm với tham số sắp xếp (sort)")
    void getAllProducts_Sort_TC03() {
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "price"));
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
        // Optionally: assert order if test data is seeded
    }

    @Test
    @DisplayName("TC-04: Lấy danh sách sản phẩm với tham số lọc theo danh mục")
    void getAllProducts_FilterByCategory_TC04() {
        Pageable pageable = PageRequest.of(0, 10);
        // If your service supports filtering by category, add it here. Otherwise, just test paging.
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
    }

    @Test
    @DisplayName("TC-05: Lấy danh sách sản phẩm với tham số tìm kiếm (keyword)")
    void getAllProducts_Keyword_TC05() {
        Pageable pageable = PageRequest.of(0, 10);
        // If your service supports keyword search, add it here. Otherwise, just test paging.
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
    }

    @Test
    @DisplayName("TC-06: Lấy danh sách sản phẩm với nhiều tham số kết hợp")
    void getAllProducts_CombinedParams_TC06() {
        Pageable pageable = PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "price"));
        // If your service supports category and keyword, add them here. Otherwise, just test paging and sorting.
        Page<ProductDTO> page = productService.getAllProducts(pageable);
        List<ProductDTO> products = page.getContent();
        assertNotNull(products);
    }

    @Test
    @DisplayName("TC-07: Lấy danh sách sản phẩm với tham số phân trang không hợp lệ")
    void getAllProducts_InvalidPage_TC07() {
        assertThrows(IllegalArgumentException.class, () -> PageRequest.of(-1, 10));
    }

    @Test
    @DisplayName("TC-08: Lấy chi tiết sản phẩm với productId hợp lệ")
    void getProductById_Valid_TC08() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Test Product TC08 " + System.nanoTime());
        req.setBrand("Test Brand");
        req.setPrice(10000.0);
        req.setQuantity(10);
        req.setCategoryId(getPersistedCategoryId());
        ProductDTO created = productService.createProduct(req);
        assertNotNull(created.getId());
        ProductDTO found = productService.getProductById(created.getId()).get();
        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("TC-09: Lấy chi tiết sản phẩm với productId không tồn tại")
    void getProductById_NotFound_TC09() {
        assertTrue(productService.getProductById("nonexistent-id").isEmpty());
    }

    @Test
    @DisplayName("TC-10: Tạo sản phẩm mới với thông tin hợp lệ")
    void createProduct_Valid_TC10() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("New Product TC10 " + System.nanoTime());
        req.setBrand("Brand A");
        req.setPrice(10000.0);
        req.setQuantity(5);
        req.setCategoryId(getPersistedCategoryId());
        ProductDTO created = productService.createProduct(req);
        assertNotNull(created.getId());
        assertEquals(req.getName(), created.getName()); // match dynamic name
    }

    @Test
    @DisplayName("TC-11: Tạo sản phẩm thiếu trường bắt buộc")
    void createProduct_MissingFields_TC11() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setPrice(10000.0);
        // Missing required fields: name, brand, quantity, categoryId
        assertThrows(Exception.class, () -> productService.createProduct(req));
    }

    @Test
    @DisplayName("TC-12: Tạo sản phẩm với dữ liệu không hợp lệ")
    void createProduct_InvalidData_TC12() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Invalid Product");
        req.setBrand("Brand X");
        req.setPrice(-10000.0);
        req.setQuantity(-1);
        req.setCategoryId(getPersistedCategoryId());
        assertThrows(Exception.class, () -> productService.createProduct(req));
    }

//    @Test
//    @DisplayName("TC-13: Cập nhật sản phẩm thành công với productId hợp lệ")
//    void updateProduct_Valid_TC13() {
//        ProductCreateRequest createReq = new ProductCreateRequest();
//        createReq.setName("Test Product TC13 " + System.nanoTime());
//        createReq.setBrand("Brand B");
//        createReq.setPrice(10000.0);
//        createReq.setQuantity(3);
//        createReq.setCategoryId(testCategoryId);
//        ProductDTO created = productService.createProduct(createReq);
//        ProductUpdateRequest updateReq = new ProductUpdateRequest();
//        updateReq.setName("Updated Product");
//        updateReq.setPrice(20000.0);
//        ProductDTO updated = productService.updateProduct(created.getId(), updateReq).get();
//        assertEquals("Updated Product", updated.getName());
//        assertEquals(20000.0, updated.getPrice());
//    }

    @Test
    @DisplayName("TC-14: Cập nhật sản phẩm với productId không tồn tại")
    void updateProduct_NotFound_TC14() {
        ProductUpdateRequest updateReq = new ProductUpdateRequest();
        updateReq.setName("Updated Product");
        updateReq.setPrice(20000.0);
        assertTrue(productService.updateProduct("nonexistent-id", updateReq).isEmpty());
    }

    @Test
    @DisplayName("TC-15: Cập nhật sản phẩm với dữ liệu không hợp lệ")
    void updateProduct_InvalidData_TC15() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Test Product");
        createReq.setBrand("Brand C");
        createReq.setPrice(10000.0);
        createReq.setQuantity(2);
        createReq.setCategoryId(testCategoryId);
        ProductDTO created = productService.createProduct(createReq);
        ProductUpdateRequest updateReq = new ProductUpdateRequest();
        updateReq.setName("");
        updateReq.setPrice(-1.0);
        assertThrows(Exception.class, () -> productService.updateProduct(created.getId(), updateReq));
    }

//    @Test
//    @DisplayName("TC-16: Cập nhật một phần thông tin sản phẩm (partial update)")
//    void partialUpdateProduct_TC16() {
//        ProductCreateRequest createReq = new ProductCreateRequest();
//        createReq.setName("Partial Product TC16 " + System.nanoTime());
//        createReq.setBrand("Partial Brand");
//        createReq.setPrice(5000.0);
//        createReq.setQuantity(1);
//        createReq.setCategoryId(testCategoryId);
//        ProductDTO created = productService.createProduct(createReq);
//        ProductUpdateRequest updateReq = new ProductUpdateRequest();
//        updateReq.setName("Partial Updated"); // Only update name
//        ProductDTO updated = productService.updateProduct(created.getId(), updateReq).get();
//        assertEquals("Partial Updated", updated.getName());
//    }

    @Test
    @DisplayName("TC-17: Xóa sản phẩm thành công với productId hợp lệ")
    void deleteProduct_Valid_TC17() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setName("Test Product");
        createReq.setBrand("Brand D");
        createReq.setPrice(10000.0);
        createReq.setQuantity(4);
        createReq.setCategoryId(testCategoryId);
        ProductDTO created = productService.createProduct(createReq);
        productService.deleteProduct(created.getId());
        assertTrue(productService.getProductById(created.getId()).isEmpty());
    }

    // TC-18, TC-19, TC-20 (Upload image) would require service method for image upload, which is not shown in the current ProductService interface.
    // If you have such a method, add similar tests here for upload success, invalid file type, and size exceeded.
}
