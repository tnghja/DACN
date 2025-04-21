package com.ecommerce.product;

import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl = "/api/products";

    @Nested
    @DisplayName("GET /api/products")
    class GetProducts {
        @Test
        @DisplayName("TC-01: Lấy danh sách sản phẩm không cần xác thực, không có filter/sort/page")
        void getAllProducts_Default() throws Exception {
            mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())))
                    .andExpect(jsonPath("$.metadata", is(notNullValue())));
        }

        @Test
        @DisplayName("TC-02: Lấy danh sách sản phẩm với tham số phân trang")
        void getAllProducts_Pagination() throws Exception {
            mockMvc.perform(get(baseUrl).param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())))
                    .andExpect(jsonPath("$.metadata.currentPage", is(1)))
                    .andExpect(jsonPath("$.metadata.pageSize", is(2)));
        }

        @Test
        @DisplayName("TC-03: Lấy danh sách sản phẩm với tham số sắp xếp (sort)")
        void getAllProducts_Sort() throws Exception {
            mockMvc.perform(get(baseUrl).param("sort", "price,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())));
            // Optionally: verify order if you have test data
        }

        @Test
        @DisplayName("TC-04: Lấy danh sách sản phẩm với tham số lọc theo danh mục")
        void getAllProducts_FilterByCategory() throws Exception {
            mockMvc.perform(get(baseUrl).param("category", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())));
        }

        @Test
        @DisplayName("TC-05: Lấy danh sách sản phẩm với tham số tìm kiếm (keyword)")
        void getAllProducts_Keyword() throws Exception {
            mockMvc.perform(get(baseUrl).param("keyword", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())));
        }

        @Test
        @DisplayName("TC-06: Lấy danh sách sản phẩm với nhiều tham số kết hợp")
        void getAllProducts_CombinedParams() throws Exception {
            mockMvc.perform(get(baseUrl)
                    .param("category", "1")
                    .param("sort", "price,asc")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(notNullValue())));
        }

        @Test
        @DisplayName("TC-07: Lấy danh sách sản phẩm với tham số phân trang không hợp lệ")
        void getAllProducts_InvalidPage() throws Exception {
            mockMvc.perform(get(baseUrl).param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductDetail {
        @Test
        @DisplayName("TC-08: Lấy chi tiết sản phẩm với productId hợp lệ")
        void getProductById_Valid() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            mockMvc.perform(get(baseUrl + "/" + productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(productId)));
        }
        @Test
        @DisplayName("TC-09: Lấy chi tiết sản phẩm với productId không tồn tại")
        void getProductById_NotFound() throws Exception {
            mockMvc.perform(get(baseUrl + "/nonexistent-id"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/products")
    class CreateProduct {
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-10: Tạo sản phẩm mới với thông tin hợp lệ (Admin)")
        void createProduct_Valid() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest();
            request.setName("New Product");
            request.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(request);
            mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("New Product")));
        }
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-11: Tạo sản phẩm thiếu trường bắt buộc")
        void createProduct_MissingFields() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest();
            request.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(request);
            mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-12: Tạo sản phẩm với dữ liệu không hợp lệ")
        void createProduct_InvalidData() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest();
            request.setName("Invalid Product");
            request.setPrice(-10000.0);
            String json = objectMapper.writeValueAsString(request);
            mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProduct {
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-13: Cập nhật sản phẩm thành công với productId hợp lệ (Admin)")
        void updateProduct_Valid() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName("Updated Product");
            updateRequest.setPrice(20000.0);
            String updateJson = objectMapper.writeValueAsString(updateRequest);
            mockMvc.perform(put(baseUrl + "/" + productId).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Updated Product")));
        }
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-14: Cập nhật sản phẩm với productId không tồn tại")
        void updateProduct_NotFound() throws Exception {
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName("Updated Product");
            updateRequest.setPrice(20000.0);
            String updateJson = objectMapper.writeValueAsString(updateRequest);
            mockMvc.perform(put(baseUrl + "/nonexistent-id").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andExpect(status().isNotFound());
        }
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-15: Cập nhật sản phẩm với dữ liệu không hợp lệ")
        void updateProduct_InvalidData() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName("");
            updateRequest.setPrice(-1.0);
            String updateJson = objectMapper.writeValueAsString(updateRequest);
            mockMvc.perform(put(baseUrl + "/" + productId).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andExpect(status().isBadRequest());
        }
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-16: Cập nhật một phần thông tin sản phẩm (partial update)")
        void partialUpdateProduct() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Partial Product");
            createRequest.setPrice(5000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            // Only update name
            String patchJson = "{\"name\": \"Partial Updated\"}";
            mockMvc.perform(patch(baseUrl + "/" + productId).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Partial Updated")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProduct {
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-17: Xóa sản phẩm thành công với productId hợp lệ (Admin)")
        void deleteProduct_Valid() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            mockMvc.perform(delete(baseUrl + "/" + productId).with(csrf())
                    .accept(String.valueOf(status().isOk())));
        }
    }

    @Nested
    @DisplayName("POST /api/products/{productId}/upload-images")
    class UploadProductImages {
        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-18: Upload hình ảnh cho sản phẩm hợp lệ (Admin)")
        void uploadProductImages_Valid() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes(StandardCharsets.UTF_8)
            );
            mockMvc.perform(MockMvcRequestBuilders.multipart(baseUrl + "/" + productId + "/upload-images")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isAccepted());
        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-19: Upload file không phải hình ảnh")
        void uploadProductImages_InvalidFileType() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    "not-image.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "not an image".getBytes(StandardCharsets.UTF_8)
            );
            mockMvc.perform(MockMvcRequestBuilders.multipart(baseUrl + "/" + productId + "/upload-images")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("TC-20: Upload hình ảnh vượt quá dung lượng cho phép")
        void uploadProductImages_TooLarge() throws Exception {
            ProductCreateRequest createRequest = new ProductCreateRequest();
            createRequest.setName("Test Product");
            createRequest.setPrice(10000.0);
            String json = objectMapper.writeValueAsString(createRequest);
            MvcResult result = mockMvc.perform(post(baseUrl).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String productId = objectMapper.readTree(response).path("data").path("id").asText();

            // Simulate a large file (e.g., 10MB)
            byte[] largeContent = new byte[10 * 1024 * 1024];
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    "large-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    largeContent
            );
            mockMvc.perform(MockMvcRequestBuilders.multipart(baseUrl + "/" + productId + "/upload-images")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest()); // Or .isPayloadTooLarge() if mapped
        }
    }
}
