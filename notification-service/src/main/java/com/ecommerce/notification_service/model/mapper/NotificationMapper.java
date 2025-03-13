package com.ecommerce.notification_service.model.mapper;

import com.ecommerce.notification_service.exception.NotFoundException;
import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.User;
import com.ecommerce.notification_service.model.entity.request.NotificationRequest;
import com.ecommerce.notification_service.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "id", ignore = true)  // Auto-generated
    @Mapping(target = "user", ignore = true) // We set it manually later
    @Mapping(target = "read", constant = "false") // Default unread
    @Mapping(target = "createdAt", ignore = true) // Handled by @CreationTimestamp
    public abstract Notification toEntity(NotificationRequest request);

    @AfterMapping
    protected void setUser(@MappingTarget Notification notification, NotificationRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));
        notification.setUser(user);
    }
}
