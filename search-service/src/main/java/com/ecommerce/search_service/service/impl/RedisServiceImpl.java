package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RedisServiceImpl implements RedisService {
    private static final String REDIS_PREFIX = "image_search:";
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheImageSearchResults(String imageHash, List<String> productIds) {
        String key = REDIS_PREFIX + imageHash;

        // Serialize List<String> to JSON or comma-separated string
        String productIdsAsString = String.join(",", productIds);

        // Set value with default TTL (from application.yml)
        redisTemplate.opsForValue().set(key, productIdsAsString);
    }

    @Override
    public List<String> getCachedImageSearchResults(String imageHash) {
        String key = REDIS_PREFIX + imageHash;
        String cachedData = redisTemplate.opsForValue().get(key);

        // Deserialize from comma-separated string
        return cachedData != null ? Arrays.asList(cachedData.split(",")) : null;
    }

    @Override
    public void invalidateAllCache() {
        String pattern = REDIS_PREFIX + "*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                redisTemplate.delete(new String(key));
            }
        }
    }
}
