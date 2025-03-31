package com.ecommerce.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadRequest {
    private String fileName;
    private byte[] fileData;
}
