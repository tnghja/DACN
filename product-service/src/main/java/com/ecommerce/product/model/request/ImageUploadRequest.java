package com.ecommerce.product.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadRequest {
    private String fileName;
    private byte[] fileData;
}
