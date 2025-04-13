package com.ecommerce.identityservice.dto.response;

import java.util.HashMap;
import java.util.Map;

public class ResponseCode {
    public static Map<String,String> getError(Integer code) {
        Map<String, String> map = new HashMap<String, String>();
        switch (code) {
            case 1:
                map.put("MSG 1", "Invalid request: missing or incorrect format");
                break;
            case 2:
                map.put("MSG 2", "User already exists in the system");
                break;
            case 3:
                map.put("MSG 3", "Verification code has expired");
                break;
            case 8:
                map.put("MSG 8", "Account does not exist");
                break;
            case 9:
                map.put("MSG 9", "Incorrect password");
                break;
            case 10:
                map.put("MSG 10", "Course not found");
                break;
            case 11:
                map.put("MSG 11", "Account already activated");
                break;
            // TODO case 4 - 22
            case 21:
                map.put("MSG 21", "An error occurred, please try again later");
                break;
            case 23:
                map.put("MSG 23", "Internal Server Error");
                break;
            case 24:
                map.put("MSG 24", "Student has not enrolled the course");
                break;
            case 25:
                map.put("MSG 25", "Already exists");
                break;
            default:
                map.put("MSG ?", "Unknown error");
                break;
        }
        return map;
    }
}
