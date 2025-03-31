package com.ecommerce.order.repository.httpClient;


import com.ecommerce.order.error_handler.ProductErrorDecoder;
import com.ecommerce.order.model.dto.InventoryRequest;
import com.ecommerce.order.model.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Repository
@FeignClient(name = "product-service",
        url = "${app.services.product}",
        configuration = ProductErrorDecoder.class
)
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductDTO getProduct(@PathVariable String productId);

    @PostMapping("/products/batch")
    List<ProductDTO>getProductsBatch(@RequestBody List<String> productIds);

    @GetMapping("/products/stock/{productId}")
    Integer getProductStock(@PathVariable String productId);

    @GetMapping("/products/inventory/confirm")
    void confirmInventory(Long id);
    @GetMapping("/products/inventory/restore")
    void restoreInventory(Long id);
    @GetMapping("/products/inventory/check")
    boolean checkInventory(List<InventoryRequest> requests);
}


