package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.response.NotificationResponse;
import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.NotificationType;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.repository.NotificationRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }
        int pageSize = Math.max(1, Math.min(limit, 30));
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(0, pageSize)
        );

        return list.stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .targetUrl(n.getTargetUrl())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .timeAgo(NotificationResponse.calculateTimeAgo(n.getCreatedAt()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long currentUserId) {
        if (notificationId == null || currentUserId == null) {
            return;
        }
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipient() != null && currentUserId.equals(notification.getRecipient().getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long currentUserId) {
        if (currentUserId == null) {
            return;
        }
        notificationRepository.markAllAsReadByRecipientId(currentUserId);
    }

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message, NotificationType type, String targetUrl) {
        if (recipient == null) {
            return;
        }
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl != null ? targetUrl : "/tasks")
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Created notification [{}] for user [{}]", type, recipient.getUsername());
    }

    @Override
    @Transactional
    public void syncDeadlineNotifications(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        List<Task> pendingTasks = taskRepository.findByAssigneeIdAndStatusNotOrderByDueDateAsc(
                user.getId(),
                TaskStatus.DONE
        );

        LocalDate today = LocalDate.now();
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);

        for (Task task : pendingTasks) {
            if (task.getDueDate() == null) {
                continue;
            }

            LocalDate due = task.getDueDate();
            String taskTargetUrl = "/tasks";

            if (due.isBefore(today)) {
                // Quá hạn
                boolean existsRecent = notificationRepository.existsByRecipientIdAndTypeAndTargetUrlAndCreatedAtAfter(
                        user.getId(),
                        NotificationType.TASK_OVERDUE,
                        taskTargetUrl,
                        oneDayAgo
                );
                if (!existsRecent) {
                    createNotification(
                            user,
                            "⚠️ Quá hạn: " + task.getTitle(),
                            "Hạn chót ngày " + due + " đã trôi qua nhưng công việc chưa hoàn thành.",
                            NotificationType.TASK_OVERDUE,
                            taskTargetUrl
                    );
                }
            } else if (due.equals(today)) {
                // Đến hạn hôm nay
                boolean existsRecent = notificationRepository.existsByRecipientIdAndTypeAndTargetUrlAndCreatedAtAfter(
                        user.getId(),
                        NotificationType.TASK_DUE_SOON,
                        taskTargetUrl,
                        oneDayAgo
                );
                if (!existsRecent) {
                    createNotification(
                            user,
                            "⏰ Đến hạn hôm nay: " + task.getTitle(),
                            "Hạn chót hoàn thành trong ngày hôm nay (" + due + ").",
                            NotificationType.TASK_DUE_SOON,
                            taskTargetUrl
                    );
                }
            }
        }
    }
}
