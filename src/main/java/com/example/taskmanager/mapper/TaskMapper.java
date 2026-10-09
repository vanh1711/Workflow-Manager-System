package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.TaskStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Lớp ánh xạ thủ công (Manual Mapper) cho đối tượng Task và các thành phần liên quan.
 * Chuyển đổi an toàn từ Entity sang DTO và ngược lại bên trong ranh giới Service Transaction.
 */
public final class TaskMapper {

    private TaskMapper() {
    }

    /**
     * Chuyển đổi Entity User sang UserSummaryResponse DTO.
     */
    public static UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name(),
            user.getRole().getLabel()
        );
    }

    /**
     * Chuyển đổi Entity Task sang TaskResponse DTO đầy đủ.
     * Tự động tính toán cờ isOverdue phục vụ việc tô đỏ công việc quá hạn trên UI.
     */
    public static TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        boolean isOverdue = task.getDueDate() != null
            && task.getDueDate().isBefore(LocalDate.now())
            && task.getStatus() != TaskStatus.DONE;

        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getPriority(),
            task.getPriority().getLabel(),
            task.getPriority().getBadgeClass(),
            task.getStatus(),
            task.getStatus().getLabel(),
            task.getStatus().getBadgeClass(),
            toUserSummary(task.getAssignee()),
            CategoryMapper.toResponse(task.getCategory()),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getVersion(),
            isOverdue
        );
    }

    /**
     * Chuyển đổi TaskRequest DTO thành Entity Task mới.
     * Quy tắc nghiệp vụ bắt buộc: Task mới tạo LUÔN có trạng thái ban đầu là TODO.
     */
    public static Task toEntity(TaskRequest request, Category category, User assignee) {
        if (request == null) {
            return null;
        }
        return Task.builder()
            .title(request.getTitle().trim())
            .description(request.getDescription())
            .dueDate(request.getDueDate())
            .priority(request.getPriority())
            .status(TaskStatus.TODO) // Luôn khởi tạo ở TODO, không phụ thuộc client
            .category(category)
            .assignee(assignee)
            .build();
    }

    /**
     * Cập nhật thông tin từ TaskRequest vào Entity Task đã tồn tại.
     * Lưu ý: KHÔNG cho phép đổi trạng thái status tại đây (đổi status phải qua luồng riêng).
     */
    public static void updateEntity(Task task, TaskRequest request, Category category, User assignee) {
        if (task == null || request == null) {
            return;
        }
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setCategory(category);
        task.setAssignee(assignee);
    }

    /**
     * Gom nhóm danh sách task thành 1 đối tượng cột Kanban (KanbanColumnResponse).
     */
    public static KanbanColumnResponse toKanbanColumn(TaskStatus status, List<TaskResponse> tasks) {
        List<TaskResponse> safeTasks = tasks != null ? tasks : Collections.emptyList();
        return new KanbanColumnResponse(
            status,
            status.getLabel(),
            status.getBadgeClass(),
            safeTasks,
            safeTasks.size()
        );
    }
}
