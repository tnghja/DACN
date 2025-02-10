package com.ecommerce.product.validation;

import com.ecommerce.product.exception.InvalidFileTypeException;

import java.util.HashSet;
import java.util.Set;


public class FileValidation {

    public static final Set<String> ALLOWED_FILE_TYPES = new HashSet<>();
    public static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>();
    public static final Set<String> ALLOWED_VIDEO_TYPES = new HashSet<>();

    static {
        ALLOWED_FILE_TYPES.add("docx");
        ALLOWED_FILE_TYPES.add("pdf");
        ALLOWED_FILE_TYPES.add("txt");
        ALLOWED_IMAGE_TYPES.add("png");
        ALLOWED_IMAGE_TYPES.add("jpg");
        ALLOWED_VIDEO_TYPES.add("mp4");
    }

    public static boolean isValidFileType(String fileName) {
        return isValidType(fileName, ALLOWED_FILE_TYPES);
    }

    public static boolean isValidImageType(String fileName) {
        return isValidType(fileName, ALLOWED_IMAGE_TYPES);
    }

    public static boolean isValidVideoType(String fileName) {
        return isValidType(fileName, ALLOWED_VIDEO_TYPES);
    }

    private static boolean isValidType(String fileName, Set<String> allowedTypes) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String fileExtension = getFileExtension(fileName);
        return allowedTypes.contains(fileExtension.toLowerCase());
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return "";
        }

        return fileName.substring(lastIndexOfDot + 1);
    }

    public static void validateFileType(String fileName) {
        if (!isValidFileType(fileName)) {
            throw new InvalidFileTypeException("Invalid file type. Allowed types are: " + ALLOWED_FILE_TYPES);
        }
    }

    public static void validateImageType(String fileName)  {
        if (!isValidImageType(fileName)) {
            throw new InvalidFileTypeException("Invalid image type. Allowed type is: " + ALLOWED_IMAGE_TYPES);
        }
    }

    public static void validateVideoType(String fileName) {
        if (!isValidVideoType(fileName)) {
            throw new InvalidFileTypeException("Invalid video type. Allowed type is: " + ALLOWED_VIDEO_TYPES);
        }
    }
}
