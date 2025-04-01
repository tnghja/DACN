package com.ecommerce.search_service.constants;

import org.springframework.http.MediaType;

import java.util.List;

public final class ImageConstants {
    public static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    ;
    private ImageConstants() {
    }


}
