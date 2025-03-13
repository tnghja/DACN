package com.ecommerce.notification_service.service.impl;

import com.ecommerce.notification_service.exception.NotFoundException;
import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.User;
import com.ecommerce.notification_service.model.entity.request.NotificationRequest;
import com.ecommerce.notification_service.model.mapper.NotificationMapper;
import com.ecommerce.notification_service.repository.NotificationRepository;
import com.ecommerce.notification_service.repository.UserRepository;
import com.ecommerce.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(NotificationRequest notificationRequest) {
        Notification notification = notificationMapper.toEntity(notificationRequest);
        Notification savedNotification = notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/notifications/" + notification.getUser().getUserId(), savedNotification);

        return savedNotification;
    }

    public List<Notification> getUserNotifications(String userId) {
        User user= userRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
