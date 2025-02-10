package com.ecommerce.product.util;

import java.io.IOException;


import com.ecommerce.product.exception.InvalidFileTypeException;
import com.ecommerce.product.service.CloudinaryService;
import com.ecommerce.product.validation.FileValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.*;
import com.ecommerce.product.constant.ContentType;

@Component
public class FileAsyncUtil {
    @Autowired
    protected CloudinaryService cloudinaryService;

    public ContentType getContentType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType.startsWith("video")) {
            return ContentType.VIDEO;
        } else if (contentType.startsWith("image")) {
            return ContentType.IMAGE;
        } else if (contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return ContentType.DOCUMENT;
        } else {
            return ContentType.UNKNOWN;
        }
    }
    @Async
    public void uploadImageAsync(Long courseId, MultipartFile file) {
        ContentType contentType = getContentType(file);
        String url = null;
        try {
            switch (contentType) {
                case IMAGE:
                    url = cloudinaryService.uploadImage(file);
                    break;
                default:
                    throw new InvalidFileTypeException("Unsupported file type, content for section must be: " + FileValidation.ALLOWED_IMAGE_TYPES);
            }
        } catch (IOException e) {
            // Handle the exception
            System.out.println("cloudinary server error");
            throw new RuntimeException(e);
        }

    }




}
