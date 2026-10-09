package com.example.taskmanager.dto.response;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết đầy đủ của một công việc.
 * Chứa sẵn các thuộc tính nhãn (label) và class màu badge (badgeClass) để tầng View Thymeleaf
 * và client API có thể hiển thị trực quan mà không cần viết switch-case xử lý giao diện.
 */
public record TaskResponse(
    Long id,
    String title,
    String description,
    LocalDate dueDate,
    Priority priority,
    String priorityLabel,
    String priorityBadgeClass,
    TaskStatus status,
    String statusLabel,
    String statusBadgeClass,
    UserSummaryResponse assignee,
    CategoryResponse category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version,
    boolean overdue
) {
}
