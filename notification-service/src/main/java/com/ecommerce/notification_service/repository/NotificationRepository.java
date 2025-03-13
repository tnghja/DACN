package com.ecommerce.notification_service.repository;

import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
