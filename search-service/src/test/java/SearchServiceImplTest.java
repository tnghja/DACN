import com.ecommerce.search_service.exception.SearchOptionsException;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.mapper.ProductMapper;
import com.ecommerce.search_service.model.request.ElasticSearchRequest;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.repository.ProductElasticsearchRepository;
import com.ecommerce.search_service.repository.ProductRepository;
import com.ecommerce.search_service.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private ProductElasticsearchRepository productElasticsearchRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testElasticSearchProducts() {
        // Giả lập ElasticSearchRequest
        ElasticSearchRequest request = new ElasticSearchRequest();
        request.setName("productName");
        request.setCategoryId(1L);
        request.setMinPrice(100.0);
        request.setMaxPrice(500.0);
        request.setMinRate(4.0);
        request.setMaxRate(5.0);
        request.setPage(1);
        request.setSize(10);
        request.setSort("price,asc");

        // Giả lập Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // Giả lập NativeQuery
        NativeQuery nativeQuery = mock(NativeQuery.class);

        // Giả lập SearchHits và SearchHit
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
        ProductDocument productDocument = new ProductDocument(); // Tạo mock ProductDocument
        when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);
        when(searchHit.getContent()).thenReturn(productDocument);

        // Giả lập ElasticsearchOperations
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Thực hiện gọi phương thức
        Page<SearchHit<ProductDocument>> result = searchService.elasticSearchProducts(request);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(NativeQuery.class), eq(ProductDocument.class), any());
    }

    @Test
    void testElasticSearchProducts_Success() {
        // Giả lập ElasticSearchRequest
        ElasticSearchRequest request = createValidRequest();

        // Giả lập Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // Giả lập SearchHits và SearchHit
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
        ProductDocument productDocument = new ProductDocument(); // Tạo mock ProductDocument
        when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);
        when(searchHit.getContent()).thenReturn(productDocument);

        // Giả lập ElasticsearchOperations
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Thực hiện gọi phương thức
        Page<SearchHit<ProductDocument>> result = searchService.elasticSearchProducts(request);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(elasticsearchOperations, times(1)).search(any(NativeQuery.class), eq(ProductDocument.class), any());
    }

    @Test
    void testElasticSearchProducts_NoResults() {
        // Giả lập ElasticSearchRequest
        ElasticSearchRequest request = createValidRequest();

        // Giả lập Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // Giả lập SearchHits trả về không có kết quả
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(searchHits.getTotalHits()).thenReturn(0L);

        // Giả lập ElasticsearchOperations
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Thực hiện gọi phương thức
        Page<SearchHit<ProductDocument>> result = searchService.elasticSearchProducts(request);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testElasticSearchProducts_InvalidRequest() {
        // Giả lập ElasticSearchRequest với tham số không hợp lệ
        ElasticSearchRequest request = new ElasticSearchRequest();
        request.setSort("invalidSort"); // Sort không hợp lệ

        // Kiểm tra khi gọi phương thức với yêu cầu không hợp lệ
        assertThrows(SearchOptionsException.class, () -> searchService.elasticSearchProducts(request));
    }

    @Test
    void testElasticSearchProducts_EmptyRequest() {
        // Giả lập ElasticSearchRequest trống
        ElasticSearchRequest request = new ElasticSearchRequest();

        // Giả lập Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // Giả lập SearchHits trả về không có kết quả
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());  // Trả về danh sách trống
        when(searchHits.getTotalHits()).thenReturn(0L);  // Tổng số kết quả là 0

        // Giả lập ElasticsearchOperations trả về giá trị searchHits
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Thực hiện gọi phương thức
        Page<SearchHit<ProductDocument>> result = searchService.elasticSearchProducts(request);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());  // Đảm bảo không có sản phẩm
    }

    @Test
    void testElasticSearchProducts_ErrorInElasticsearch() {
        // Giả lập ElasticSearchRequest
        ElasticSearchRequest request = createValidRequest();

        // Giả lập Pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        // Giả lập ElasticsearchOperations throw exception
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenThrow(new RuntimeException("Elasticsearch error"));

        // Kiểm tra khi có lỗi trong Elasticsearch
        assertThrows(RuntimeException.class, () -> searchService.elasticSearchProducts(request));
    }

    // Phương thức tạo request hợp lệ
    private ElasticSearchRequest createValidRequest() {
        ElasticSearchRequest request = new ElasticSearchRequest();
        request.setName("productName");
        request.setCategoryId(1L);
        request.setMinPrice(100.0);
        request.setMaxPrice(500.0);
        request.setMinRate(4.0);
        request.setMaxRate(5.0);
        request.setPage(1);
        request.setSize(10);
        request.setSort("price,asc");
        return request;
    }
    @Test
    void testAutocompleteProductNames_NoResults() {
        String prefix = "NonExistentPrefix";

        // Giả lập Elasticsearch trả về không có kết quả
        SearchHits<ProductDocument> emptySearchHits = mock(SearchHits.class);
        when(emptySearchHits.getSearchHits()).thenReturn(Collections.emptyList());

        // Giả lập elasticsearchOperations.search()
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(emptySearchHits);

        // Gọi phương thức và kiểm tra kết quả
        List<String> result = searchService.autocompleteProductNames(prefix);

        assertNotNull(result);
        assertTrue(result.isEmpty());  // Đảm bảo danh sách gợi ý là rỗng
    }
    @Test
    void testAutocompleteProductNames_ValidResults() {
        String prefix = "Product";

        // Giả lập dữ liệu ProductDocument
        ProductDocument product1 = new ProductDocument();
        product1.setName("Product A");

        ProductDocument product2 = new ProductDocument();
        product2.setName("Product B");

        // Tạo SearchHit mock cho mỗi sản phẩm
        SearchHit<ProductDocument> searchHit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> searchHit2 = mock(SearchHit.class);

        // Cung cấp dữ liệu cho SearchHit
        when(searchHit1.getContent()).thenReturn(product1);
        when(searchHit2.getContent()).thenReturn(product2);

        // Giả lập SearchHits với một danh sách các SearchHit
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit1, searchHit2));

        // Giả lập elasticsearchOperations.search()
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Gọi phương thức và kiểm tra kết quả
        List<String> result = searchService.autocompleteProductNames(prefix);

        assertNotNull(result);
        assertEquals(2, result.size());  // Đảm bảo có 2 gợi ý
        assertTrue(result.contains("Product A"));
        assertTrue(result.contains("Product B"));
    }
    @Test
    void testAutocompleteProductNames_DuplicateResults() {
        String prefix = "Product";

        // Giả lập dữ liệu bị trùng
        ProductDocument product1 = new ProductDocument();
        product1.setName("Product A");

        ProductDocument product2 = new ProductDocument();
        product2.setName("Product A"); // Sản phẩm trùng tên

        // Mock SearchHit cho từng sản phẩm
        SearchHit<ProductDocument> searchHit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> searchHit2 = mock(SearchHit.class);

        when(searchHit1.getContent()).thenReturn(product1);
        when(searchHit2.getContent()).thenReturn(product2);

        // Mock SearchHits trả về kết quả trùng
        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit1, searchHit2));

        // Mock Elasticsearch search query
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class), any()))
                .thenReturn(searchHits);

        // Gọi phương thức và kiểm tra kết quả
        List<String> result = searchService.autocompleteProductNames(prefix);

        assertNotNull(result);
        assertEquals(1, result.size());  // Chỉ có 1 kết quả do loại bỏ trùng lặp
        assertTrue(result.contains("Product A"));
    }

}