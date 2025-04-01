package com.ecommerce.search_service.service;

import java.util.List;

public interface RedisService {

    // Lưu danh sách product_id vào Redis, TTL sẽ được quản lý bởi cấu hình trong application.yml
    public void cacheImageSearchResults(String imageHash, List<String> productIds);

    // Lấy danh sách product_id từ Redis
    public List<String> getCachedImageSearchResults(String imageHash);
}
