package com.example.taskmanager.controller.api;

import com.example.taskmanager.common.ApiResponse;
import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.common.validation.OnCreate;
import com.example.taskmanager.common.validation.OnUpdate;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * RESTful API Controller quản lý Công việc và Luồng tác vụ (Task & Workflow).
 * Cung cấp các endpoint chuẩn JSON theo prefix {@code /api/v1/tasks}.
 *
 * Nguyên tắc thiết kế Controller mỏng (Thin Controller):
 * - Controller chỉ chịu trách nhiệm nhận request, kích hoạt validation đầu vào,
 *   ủy quyền toàn bộ xử lý cho TaskService và đóng gói kết quả vào ResponseEntity.
 * - Không chứa bất kỳ câu lệnh logic nghiệp vụ hay truy vấn database trực tiếp nào.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskApiController {

    private final TaskService taskService;

    /**
     * Lấy danh sách công việc kết hợp lọc đa tiêu chí và phân trang.
     * GET /api/v1/tasks?keyword=...&status=...&priority=...&page=0&size=10&sortBy=createdAt&sortDirection=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasks(
        @ModelAttribute TaskFilterRequest filter,
        HttpServletRequest request
    ) {
        PageResponse<TaskResponse> response = taskService.getTasks(filter);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy danh sách công việc thành công", request.getRequestURI()));
    }

    /**
     * Lấy dữ liệu 4 cột trên bảng Kanban.
     * GET /api/v1/tasks/kanban?categoryId=...&assigneeId=...
     */
    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<List<KanbanColumnResponse>>> getKanbanBoard(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long assigneeId,
        HttpServletRequest request
    ) {
        List<KanbanColumnResponse> response = taskService.getKanbanBoard(categoryId, assigneeId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy dữ liệu bảng Kanban thành công", request.getRequestURI()));
    }

    /**
     * Lấy thông tin chi tiết một công việc theo ID.
     * GET /api/v1/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết công việc thành công", request.getRequestURI()));
    }

    /**
     * Tạo mới công việc.
     * POST /api/v1/tasks
     * Áp dụng nhóm validation {@link OnCreate}: Bắt buộc dueDate không được để trống và phải từ hôm nay trở đi.
     * Trả về HTTP 201 CREATED kèm header Location trỏ đến URI của tài nguyên vừa tạo.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
        @Validated(OnCreate.class) @RequestBody TaskRequest taskRequest,
        HttpServletRequest request
    ) {
        TaskResponse created = taskService.createTask(taskRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location)
            .body(ApiResponse.created(created, "Tạo công việc mới thành công", request.getRequestURI()));
    }

    /**
     * Cập nhật toàn bộ nội dung công việc (Idempotent).
     * PUT /api/v1/tasks/{id}
     * Áp dụng nhóm validation {@link OnUpdate}: Cho phép sửa task đã quá hạn mà không bị chặn.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
        @PathVariable Long id,
        @Validated(OnUpdate.class) @RequestBody TaskRequest taskRequest,
        HttpServletRequest request
    ) {
        TaskResponse updated = taskService.updateTask(id, taskRequest);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật công việc thành công", request.getRequestURI()));
    }

    /**
     * Cập nhật trạng thái công việc (Partial Update / State Transition).
     * PATCH /api/v1/tasks/{id}/status
     * Kiểm thực quy tắc chuyển đổi trạng thái bằng Task State Machine (ném HTTP 422 nếu vi phạm).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody TaskStatusUpdateRequest statusRequest,
        HttpServletRequest request
    ) {
        TaskResponse updated = taskService.changeStatus(id, statusRequest);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật trạng thái công việc thành công", request.getRequestURI()));
    }

    /**
     * Xóa công việc theo ID.
     * DELETE /api/v1/tasks/{id}
     * Trả về HTTP 204 NO CONTENT chuẩn REST khi xóa thành công.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
