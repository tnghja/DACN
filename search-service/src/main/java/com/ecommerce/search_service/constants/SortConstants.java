package com.ecommerce.search_service.constants;

import java.util.List;
import java.util.Map;

public final class SortConstants {
    private SortConstants() {} // Private constructor to prevent instantiation

    public static final List<String> AVAILABLE_SORT_FIELDS = List.of("price", "rate", "quantity");
    public static final List<String> SORT_DIRECTIONS = List.of("asc", "desc");
    public static final String SORT_SYNTAX = "field,direction (e.g., price,asc or rate,desc)";

    public static Map<String, Object> getSortMetadata() {
        return Map.of(
                "availableFields", AVAILABLE_SORT_FIELDS,
                "syntax", SORT_SYNTAX,
                "directions", SORT_DIRECTIONS
        );
    }
}