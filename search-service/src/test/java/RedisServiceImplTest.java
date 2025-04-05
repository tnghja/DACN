import com.ecommerce.search_service.service.impl.RedisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCacheImageSearchResults() {
        String imageHash = "testHash";
        List<String> productIds = Arrays.asList("p1", "p2", "p3");
        String key = "image_search:" + imageHash;
        String value = "p1,p2,p3";

        // Act
        redisService.cacheImageSearchResults(imageHash, productIds);

        // Assert
        verify(valueOperations).set(key, value);
    }

    @Test
    void testGetCachedImageSearchResults_WhenDataExists() {
        String imageHash = "testHash";
        String key = "image_search:" + imageHash;
        String cachedValue = "p1,p2,p3";

        when(valueOperations.get(key)).thenReturn(cachedValue);

        // Act
        List<String> result = redisService.getCachedImageSearchResults(imageHash);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("p1", result.get(0));
        assertEquals("p2", result.get(1));
        assertEquals("p3", result.get(2));
    }

    @Test
    void testGetCachedImageSearchResults_WhenDataNotExists() {
        String imageHash = "testHash";
        String key = "image_search:" + imageHash;

        when(valueOperations.get(key)).thenReturn(null);

        // Act
        List<String> result = redisService.getCachedImageSearchResults(imageHash);

        // Assert
        assertNull(result);
    }
}