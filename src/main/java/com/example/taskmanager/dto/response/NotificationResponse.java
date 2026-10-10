package com.example.taskmanager.dto.response;

import com.example.taskmanager.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private String targetUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private String timeAgo;

    public static String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Vừa xong";
        }
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return "Vừa xong";
        }
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " phút trước";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }
        long days = duration.toDays();
        if (days < 30) {
            return days + " ngày trước";
        }
        return dateTime.toLocalDate().toString();
    }
}
