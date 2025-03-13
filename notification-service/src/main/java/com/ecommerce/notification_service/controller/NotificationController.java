package com.ecommerce.notification_service.controller;

import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.request.NotificationRequest;
import com.ecommerce.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        Notification savedNotification = notificationService.createNotification(request);
        return ResponseEntity.ok(savedNotification);
    }

    @PostMapping("/{id}/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}