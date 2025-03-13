package com.ecommerce.product.service;


import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.model.entity.User;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.Batch;
import com.recombee.api_client.api_requests.Request;
import com.recombee.api_client.api_requests.SetItemValues;
import com.recombee.api_client.api_requests.SetUserValues;
import com.recombee.api_client.exceptions.ApiException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecombeeSyncService {
    private final RecombeeClient recombeeClient;

    public RecombeeSyncService(RecombeeClient recombeeClient) {
        this.recombeeClient = recombeeClient;
    }


    public void syncProduct(Product product) {
        try {
            recombeeClient.send(new SetItemValues(
                    product.getId(),
                    new HashMap<String, Object>() {{
                        put("name", product.getName());
                        put("brand", product.getBrand());
                        put("cover", product.getCover());
                        put("price", product.getPrice());
                        put("rate", product.getRate());
//                        put("category", product.getCategory());
                    }}).setCascadeCreate(true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đồng bộ danh sách sản phẩm lên Recombee (batch)
    public void batchSyncProducts(List<Product> products) {
        try {
            List<Request> requests = products.stream()
                    .map(product -> new SetItemValues(
                            product.getId(),
                            new HashMap<String, Object>() {{
                                put("name", product.getName());
                                put("brand", product.getBrand());
                                put("cover", product.getCover());
                                put("description", product.getDescription());
                                put("price", product.getPrice());
                                put("quantity", product.getQuantity());
                                put("rate", product.getRate());
                                put("category", product.getCategory());
                            }}
                    ).setCascadeCreate(true))
                    .collect(Collectors.toList());

            recombeeClient.send(new Batch(requests));
        } catch (ApiException e) {
            e.printStackTrace();
            // Handle the exception, e.g., log or use fallback
        }
    }


    public void syncUser(User user) {
        try {
            recombeeClient.send(new SetUserValues(
                    String.format("user-%s", user.getId().toString()),
                    new HashMap<String, Object>() {{
                        put("name", user.getName());
                        put("email", user.getEmail());
                        put("phoneNumber", user.getPhoneNumber());
                        put("avatarUrl", user.getAvatarUrl());
                        put("createAt", user.getCreateAt());
                    }}).setCascadeCreate(true));
        } catch (ApiException e) {
            e.printStackTrace();
            // Handle the exception, e.g., log or use fallback
        }
    }

    // Đồng bộ danh sách user lên Recombee (batch)
    public void batchSyncUsers(List<User> users) {
        try {
            List<Request> requests = users.stream()
                    .map(user -> new SetUserValues(
                            String.format("user-%s", user.getId().toString()),
                            new HashMap<String, Object>() {{
                                put("name", user.getName());
                                put("email", user.getEmail());
                                put("phoneNumber", user.getPhoneNumber());
                                put("avatarUrl", user.getAvatarUrl());
                                put("createAt", user.getCreateAt());
                            }}).setCascadeCreate(true)).collect(Collectors.toList());


            recombeeClient.send(new Batch(requests));
        } catch (ApiException e) {
            e.printStackTrace();
            // Handle the exception, e.g., log or use fallback
        }
    }
}
