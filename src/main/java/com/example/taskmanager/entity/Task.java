package com.example.taskmanager.entity;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Thực thể trung tâm đại diện cho Công việc / Tác vụ trong hệ thống (Task Entity).
 * Thiết kế tối ưu cho môi trường Enterprise & Banking:
 * - Quan hệ {@code @ManyToOne} luôn dùng {@code fetch = FetchType.LAZY} để ngăn chặn việc
 *   tự động eager load (JOIN thừa thãi) gây suy giảm hiệu năng cơ sở dữ liệu.
 * - Thêm các chỉ mục (Indexes) tại database level cho các trường thường xuyên được lọc và sắp xếp
 *   (status, due_date, category_id) giúp tăng tốc độ truy vấn trên tập dữ liệu lớn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_status", columnList = "status"),
        @Index(name = "idx_tasks_due_date", columnList = "due_date"),
        @Index(name = "idx_tasks_category_id", columnList = "category_id")
    }
)
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    /**
     * Người được giao việc (Assignee). Có thể null nếu công việc chưa được phân công.
     * Dùng LAZY loading để không truy vấn bảng users khi chỉ cần đọc thông tin task.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Trì hoãn nạp dữ liệu: chỉ truy vấn User khi thực sự gọi getAssignee()
    @JoinColumn(name = "user_id")
    private User assignee;

    /**
     * Danh mục / Dự án của công việc. Bắt buộc phải có (nullable = false).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
