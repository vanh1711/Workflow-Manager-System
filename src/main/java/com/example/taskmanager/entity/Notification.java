package com.example.taskmanager.entity;

import com.example.taskmanager.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Thực thể đại diện cho thông báo người dùng trong hệ thống (Notification Entity).
 * Lưu trữ thông báo giao việc, hạn chót, thay đổi trạng thái và cập nhật hệ thống.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_is_read", columnList = "is_read"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
    }
)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "target_url", length = 255)
    private String targetUrl;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
}
