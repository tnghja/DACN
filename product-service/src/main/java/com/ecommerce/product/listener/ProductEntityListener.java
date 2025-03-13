package com.ecommerce.product.listener;

import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.service.RecombeeSyncService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityListener {

    private static RecombeeSyncService recombeeSyncService;

    @Autowired
    public void setRecombeeSyncService(RecombeeSyncService service) {
        recombeeSyncService = service;
    }

    @PostPersist
    @PostUpdate
    public void onProductChange(Product product) {
        System.out.println("Product changed: " + product.getId());
        recombeeSyncService.syncProduct(product);
    }
}
