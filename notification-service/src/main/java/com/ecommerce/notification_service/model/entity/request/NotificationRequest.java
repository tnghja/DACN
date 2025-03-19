package com.ecommerce.notification_service.model.entity.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String userId;
    private String message;
    private String type; // "PUSH", "EMAIL", "SMS"
}