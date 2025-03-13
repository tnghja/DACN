package com.ecommerce.product.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.product.exception.InvalidFileTypeException;
import com.ecommerce.product.service.CloudinaryService;
import com.ecommerce.product.validation.FileValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    public String uploadImage(byte[] fileData, String fileName) throws IOException, InvalidFileTypeException {
        FileValidation.validateImageType(fileName);
        Map uploadResult = cloudinary.uploader().upload(fileData, ObjectUtils.asMap(
                "resource_type", "raw"
        ));
        return uploadResult.get("url").toString();
    }

    public String uploadFile(byte[] file) throws IOException, InvalidFileTypeException {
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("resource_type", "video"));
        return uploadResult.get("url").toString();
    }

    public String uploadFile(MultipartFile file) throws IOException, InvalidFileTypeException {
        FileValidation.validateFileType(file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }

    public String uploadImage(MultipartFile file) throws IOException, InvalidFileTypeException {
        FileValidation.validateImageType(file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "raw"
        ));
        return uploadResult.get("url").toString();
    }

    public String uploadVideo(MultipartFile file) throws IOException, InvalidFileTypeException {
        FileValidation.validateVideoType(file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "video"));
        return uploadResult.get("url").toString();
    }

    public String deleteFile(String publicId) throws IOException {
        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return deleteResult.get("result").toString();
    }

    public void updateFile(String publicId, byte[] file) throws IOException, InvalidFileTypeException {

        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "resource_type", "video",
                "public_id", publicId
        ));
        uploadResult.get("url").toString();
    }

    public String updateImage(String publicId, MultipartFile file) throws IOException, InvalidFileTypeException {
        FileValidation.validateImageType(file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId
        ));
        return uploadResult.get("url").toString();
    }

    public String updateVideo(String publicId, MultipartFile file) throws IOException, InvalidFileTypeException {
        FileValidation.validateVideoType(file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "video",
                "public_id", publicId
        ));
        return uploadResult.get("url").toString();
    }

}
