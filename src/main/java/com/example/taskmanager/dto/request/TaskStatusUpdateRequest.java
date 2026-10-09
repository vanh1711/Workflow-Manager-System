package com.example.taskmanager.dto.request;

import com.example.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO nhận yêu cầu thay đổi trạng thái của Task.
 * Phục vụ cả API PATCH /api/v1/tasks/{id}/status và thao tác kéo-thả trên bảng Kanban.
 */
public record TaskStatusUpdateRequest(
    @NotNull(message = "Trạng thái mới không được để trống")
    TaskStatus status,

    Long version // Version phục vụ Optimistic Locking khi nhiều người kéo thả cùng lúc
) {
}
