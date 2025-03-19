package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.User;
import com.ecommerce.notification_service.model.entity.request.NotificationRequest;

import java.util.List;

public interface NotificationService {
    public Notification createNotification(NotificationRequest notificationRequest);
    public List<Notification> getUserNotifications(String userId);
    public void markAsRead(Long notificationId);
}
