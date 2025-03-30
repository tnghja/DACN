package com.ecommerce.search_service.service.impl;

import co.elastic.clients.json.JsonData;
import com.ecommerce.search_service.exception.SearchOptionsException;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.mapper.ProductMapper;
import com.ecommerce.search_service.model.request.ElasticSearchRequest;
import com.ecommerce.search_service.model.response.ProductResponse;

import com.ecommerce.search_service.repository.ProductRepository;
import com.ecommerce.search_service.repository.ProductSpecification;
import com.ecommerce.search_service.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ecommerce.search_service.constants.SortConstants.AVAILABLE_SORT_FIELDS;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<ProductResponse> searchProducts(String name, String categoryName, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {
        Page<Product> products = productRepository.findAll(ProductSpecification.filterBy(name, categoryName, minPrice, maxPrice, minRate, maxRate), pageable);
        return products.map(productMapper::toResponse);
    }

    private Pageable buildPageable(ElasticSearchRequest request) {
        int pageIndex = request.getPage() - 1; // Chuyển từ 1-based sang 0-based
        int size = request.getSize();

        if (request.getSort() != null) {
            String[] sortParts = request.getSort().split(",");
            if (sortParts.length != 2) {
                throw new SearchOptionsException("Invalid sort format. Use 'field,direction' (e.g., price,asc)");
            }

            String field = sortParts[0];
            String direction = sortParts[1];

            if (!AVAILABLE_SORT_FIELDS.contains(field)) {
                throw new SearchOptionsException("Invalid sort field: " + field);
            }

            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.fromString(direction), field));
        }

        return PageRequest.of(pageIndex, size);
    }
    public Page<SearchHit<ProductDocument>> elasticSearchProducts(ElasticSearchRequest request) {
        Pageable pageable = buildPageable(request);

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // Tìm kiếm với ưu tiên khớp chính xác và tiền tố
                            if (request.getName() != null && !request.getName() .isEmpty()) {
                                b.must(m -> m
                                        .bool(b2 -> b2
                                                .should(s -> s
                                                        .matchPhrasePrefix(mp -> mp
                                                                .field("name")
                                                                .query(request.getName() )
                                                                .boost(20.0f)
                                                        )
                                                )
                                                .should(s -> s
                                                        .multiMatch(mm -> mm
                                                                .query(request.getName() )
                                                                .fields(List.of("name^5", "description^2", "brand^3"))
                                                                .fuzziness("AUTO")
                                                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                                        )
                                                )
                                                .minimumShouldMatch("1")
                                        )
                                );
                            }
                        if (request.getCategoryId() != null) {
                            b.filter(f -> f.term(t -> t.field("category_id").value(request.getCategoryId())));
                        }
                        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                            b.filter(f -> f.range(r -> {
                                r.field("price");
                                if (request.getMinPrice() != null) r.gte(JsonData.of(request.getMinPrice()));
                                if (request.getMaxPrice() != null) r.lte(JsonData.of(request.getMaxPrice()));
                                return r;
                            }));
                        }
                        if (request.getMinRate() != null || request.getMaxRate() != null) {
                            b.filter(f -> f.range(r -> {
                                r.field("rate");
                                if (request.getMinRate() != null) r.gte(JsonData.of(request.getMinRate()));
                                if (request.getMaxRate() != null) r.lte(JsonData.of(request.getMaxRate()));
                                return r;
                            }));
                        }
                        return b;
                }))
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                searchQuery,
                ProductDocument.class,
                IndexCoordinates.of("postgres.public.product")
        );

        return new PageImpl<>(searchHits.getSearchHits(), pageable, searchHits.getTotalHits());
    }
    @Override
    public List<ProductResponse> findProductsByIds(List<String> productIds) {
        Criteria criteria = new Criteria("id").in(productIds);
        CriteriaQuery query = new CriteriaQuery(criteria);

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
        System.out.println(searchHits);
        // Map results to ProductResponse
        return searchHits.getSearchHits().stream()
                .map(hit -> productMapper.toResponse(hit.getContent()))
                .collect(Collectors.toList());
    }
    @Override
    public List<String> autocompleteProductNames(String prefix) {
        NativeQuery autocompleteQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .prefix(p -> p
                                .field("name")
                                .value(prefix)
                                .caseInsensitive(true)
                        )
                )
                .withPageable(Pageable.ofSize(10)) // Giới hạn 10 gợi ý
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                autocompleteQuery,
                ProductDocument.class,
                IndexCoordinates.of("postgres.public.product")
        );

        // Trích xuất danh sách tên sản phẩm từ kết quả
        return searchHits.getSearchHits().stream()
                .map(searchHit -> searchHit.getContent().getName())
                .distinct() // Loại bỏ trùng lặp
                .collect(Collectors.toList());
    }
}