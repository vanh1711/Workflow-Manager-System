package com.example.taskmanager.dto.response;

import com.example.taskmanager.enums.TaskStatus;

import java.util.List;

/**
 * DTO trả về dữ liệu của 1 cột trên bảng Kanban.
 * Gom nhóm danh sách các Task theo từng trạng thái tương ứng.
 */
public record KanbanColumnResponse(
    TaskStatus status,
    String statusLabel,
    String badgeClass,
    List<TaskResponse> tasks,
    int count
) {
}
