package com.ecommerce.search_service.service.impl;

import co.elastic.clients.json.JsonData;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.mapper.ProductMapper;
import com.ecommerce.search_service.model.response.ProductResponse;

import com.ecommerce.search_service.repository.ProductRepository;
import com.ecommerce.search_service.repository.ProductSpecification;
import com.ecommerce.search_service.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

//    @Override
//    public Page<ProductDocument> elasticSearchProducts(
//            String name, Long categoryId, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {
//
//        // Build the criteria for the query
//        Criteria criteria = new Criteria();
//
//        if (name != null && !name.isEmpty()) {
//            criteria.and(new Criteria("name").fuzzy(name));
//        }
//
//        if (categoryId != null) {
//            criteria.and(new Criteria("categoryId").is(categoryId));
//        }
//
//        if (minPrice != null) {
//            criteria.and(new Criteria("price").greaterThanEqual(minPrice));
//        }
//
//        if (maxPrice != null) {
//            criteria.and(new Criteria("price").lessThanEqual(maxPrice));
//        }
//
//        if (minRate != null) {
//            criteria.and(new Criteria("rate").greaterThanEqual(minRate));
//        }
//
//        if (maxRate != null) {
//            criteria.and(new Criteria("rate").lessThanEqual(maxRate));
//        }
//
//        // Create the query with pagination
//        Query query = new CriteriaQuery(criteria).setPageable(pageable);
//
//        // Execute the search
//        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
//
//        // Extract the content (results for the current page)
//        List<ProductDocument> content = searchHits.stream()
//                .map(hit -> hit.getContent())
//                .collect(Collectors.toList());
//
//        // Get the total number of elements (for pagination)
//        long totalElements = searchHits.getTotalHits();
//
//        // Create and return a Page object
//        return new PageImpl<>(content, pageable, totalElements);
//    }
    public Page<SearchHit<ProductDocument>> elasticSearchProducts(
            String name, Long categoryId, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {

        // Xây dựng Highlight
        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withPreTags("<strong>")
                .withPostTags("</strong>")
                .build();

        Highlight highlight = new Highlight(
                highlightParameters,
                List.of(
                        new HighlightField("name"),
                        new HighlightField("description")
                )
        );

        // Xây dựng NativeQuery với MultiMatch và Filter
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // Tìm kiếm với ưu tiên khớp chính xác và tiền tố
                            if (name != null && !name.isEmpty()) {
                                b.must(m -> m
                                        .bool(b2 -> b2
                                                .should(s -> s
                                                        .matchPhrasePrefix(mp -> mp
                                                                .field("name")
                                                                .query(name)
                                                                .boost(20.0f)
                                                        )
                                                )
                                                .should(s -> s
                                                        .multiMatch(mm -> mm
                                                                .query(name)
                                                                .fields(List.of("name^5", "description^2", "brand^3"))
                                                                .fuzziness("AUTO")
                                                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                                        )
                                                )
                                                .minimumShouldMatch("1")
                                        )
                                );
                            }
                            // Filter cho categoryId
                            if (categoryId != null) {
                                b.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                            }
                            // Filter cho price range với JsonData
                            if (minPrice != null || maxPrice != null) {
                                b.filter(f -> f.range(r -> {
                                    r.field("price");
                                    if (minPrice != null) r.gte(JsonData.of(minPrice));
                                    if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                                    return r;
                                }));
                            }
                            // Filter cho rate range với JsonData
                            if (minRate != null || maxRate != null) {
                                b.filter(f -> f.range(r -> {
                                    r.field("rate");
                                    if (minRate != null) r.gte(JsonData.of(minRate));
                                    if (maxRate != null) r.lte(JsonData.of(maxRate));
                                    return r;
                                }));
                            }
                            return b;
                        })
                )
                .withHighlightQuery(new HighlightQuery(highlight, ProductDocument.class))
                .withPageable(pageable) // Pageable đã bao gồm sort
                .build();

        // Thực hiện truy vấn lấy SearchHits
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                searchQuery,
                ProductDocument.class,
                IndexCoordinates.of("postgres.public.product")
        );

        // Chuyển đổi SearchHits sang Page<SearchHit<ProductDocument>>
        List<SearchHit<ProductDocument>> searchHitList = searchHits.getSearchHits();
        return new PageImpl<>(searchHitList, pageable, searchHits.getTotalHits());
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