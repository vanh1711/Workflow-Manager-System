package com.example.taskmanager.service;

import com.example.taskmanager.dto.response.NotificationResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getRecentNotifications(Long userId, int limit);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long currentUserId);

    void markAllAsRead(Long currentUserId);

    void createNotification(User recipient, String title, String message, NotificationType type, String targetUrl);

    void syncDeadlineNotifications(User user);
}
