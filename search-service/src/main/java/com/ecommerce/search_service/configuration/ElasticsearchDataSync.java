//package com.ecommerce.search_service.configuration;
//
//
//import com.ecommerce.search_service.model.entity.ProductDocument;
//import com.ecommerce.search_service.repository.ProductElasticsearchRepository;
//import com.ecommerce.search_service.repository.ProductRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//@Component
//@RequiredArgsConstructor
//public class ElasticsearchDataSync implements CommandLineRunner {
//
//    private final ProductRepository productRepository;
//    private final ProductElasticsearchRepository productElasticsearchRepository;
//
//    @Override
//    public void run(String... args) {
//        if (productElasticsearchRepository.count() > 0) {
//            System.out.println("Dữ liệu đã tồn tại trong Elasticsearch. Bỏ qua đồng bộ.");
//            return;
//        }
//
//        List<ProductDocument> productDocuments = productRepository.findAll().stream()
//                .map(product -> ProductDocument.builder()
//                        .id(product.getId())
//                        .name(product.getName())
//                        .brand(product.getBrand())
//                        .cover(product.getCover())
//                        .description(product.getDescription())
//                        .price(product.getPrice())
//                        .quantity(product.getQuantity())
//                        .rate(product.getRate())
//                        .categoryName(product.getCategory().getName())
//                        .build())
//                .collect(Collectors.toList());
//
//        productElasticsearchRepository.saveAll(productDocuments);
//        System.out.println("Đồng bộ dữ liệu từ PostgreSQL lên Elasticsearch thành công!");
//    }
//}
