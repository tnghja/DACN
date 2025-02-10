package com.ecommerce.product.service;


import com.ecommerce.product.exception.InvalidFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    public String uploadFile(byte[] file) throws IOException, InvalidFileTypeException;
    String uploadFile(MultipartFile file) throws IOException, InvalidFileTypeException;
    String uploadImage(MultipartFile file) throws IOException, InvalidFileTypeException;
    String uploadVideo(MultipartFile file) throws IOException, InvalidFileTypeException;
    void updateFile(String publicId, byte[] file) throws IOException, InvalidFileTypeException;
    String updateVideo(String publicId, MultipartFile file) throws IOException, InvalidFileTypeException;
    String updateImage(String publicId, MultipartFile file) throws IOException, InvalidFileTypeException;
    String deleteFile(String publicId) throws IOException;
}
