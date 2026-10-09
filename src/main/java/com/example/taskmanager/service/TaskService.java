package com.example.taskmanager.service;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.enums.TaskStatus;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ cốt lõi của Hệ thống Quản lý Công việc (Task & Workflow).
 * Cả TaskApiController (JSON) và TaskViewController (Thymeleaf) đều tái sử dụng 100% interface này.
 */
public interface TaskService {

    /**
     * Lấy danh sách công việc kết hợp phân trang, sắp xếp an toàn và lọc đa tiêu chí.
     */
    PageResponse<TaskResponse> getTasks(TaskFilterRequest filter);

    /**
     * Lấy chi tiết công việc theo ID.
     */
    TaskResponse getTaskById(Long id);

    /**
     * Tạo mới công việc.
     * Quy tắc nghiệp vụ: Trạng thái ban đầu luôn được thiết lập là TODO.
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Cập nhật nội dung công việc.
     * Quy tắc nghiệp vụ:
     * - Không cho phép chỉnh sửa nếu task đã ở trạng thái DONE (HTTP 409).
     * - Chỉ kiểm tra hạn chót >= hôm nay nếu hạn chót có sự thay đổi.
     */
    TaskResponse updateTask(Long id, TaskRequest request);

    /**
     * Chuyển đổi trạng thái công việc (State Transition).
     * Quy tắc nghiệp vụ: Bắt buộc tuân thủ State Machine của TaskStatus (ném HTTP 422 nếu vi phạm).
     */
    TaskResponse changeStatus(Long id, TaskStatusUpdateRequest request);

    /**
     * Xóa công việc theo ID.
     */
    void deleteTask(Long id);

    /**
     * Lấy dữ liệu 4 cột trên bảng Kanban (có thể lọc theo danh mục hoặc người được giao).
     */
    List<KanbanColumnResponse> getKanbanBoard(Long categoryId, Long assigneeId);

    // =========================================================================
    // CÁC HÀM TRUY VẤN THỐNG KÊ DASHBOARD
    // =========================================================================

    long countByStatus(TaskStatus status);

    long countOverdueTasks();

    long countUpcomingTasks();
}
