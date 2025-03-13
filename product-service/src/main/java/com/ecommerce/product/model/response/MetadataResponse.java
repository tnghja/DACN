package com.ecommerce.product.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataResponse {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int itemsPerPage;
    private String nextPage;
    private String previousPage;
    private String lastPage;
    private String firstPage;

    public MetadataResponse(long totalItems, int totalPages, int currentPage, int itemsPerPage,
                            String nextPage, String previousPage, String lastPage, String firstPage) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.itemsPerPage = itemsPerPage;
        this.nextPage = nextPage;
        this.previousPage = previousPage;
        this.lastPage = lastPage;
        this.firstPage = firstPage;
    }
}



