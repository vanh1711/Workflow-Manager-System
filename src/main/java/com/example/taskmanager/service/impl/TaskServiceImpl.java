package com.example.taskmanager.service.impl;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ErrorCode;
import com.example.taskmanager.exception.InvalidStatusTransitionException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.spec.TaskSpecification;
import com.example.taskmanager.enums.NotificationType;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Tầng nghiệp vụ cốt lõi cho Task — nơi DUY NHẤT chứa các quy tắc nghiệp vụ và luật chuyển trạng thái.
 * Cả TaskApiController (JSON) và TaskViewController (Thymeleaf) đều gọi lớp này,
 * nhờ vậy luật nghiệp vụ không bị viết lặp ở 2 nơi (Single Responsibility & DRY).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Mặc định chỉ đọc: Hibernate tắt dirty-checking, giải phóng tài nguyên CPU/RAM
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public PageResponse<TaskResponse> getTasks(TaskFilterRequest filter) {
        if (filter == null) {
            filter = new TaskFilterRequest();
        }

        // Kiểm tra hợp lệ khoảng ngày lọc
        if (!filter.isValidDateRange()) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        Specification<Task> spec = TaskSpecification.build(filter);
        Pageable pageable = filter.toPageable();

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        return PageResponse.from(taskPage.map(TaskMapper::toResponse));
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return TaskMapper.toResponse(task);
    }

    @Override
    @Transactional // Ghi đè: Phương thức ghi cần transaction đọc-ghi
    public TaskResponse createTask(TaskRequest request) {
        Category category = findCategoryOrThrow(request.getCategoryId());
        User assignee = findAssigneeIfPresent(request.getAssigneeId());

        // Quy tắc: Task mới luôn ở trạng thái TODO, được ép buộc bên trong TaskMapper.toEntity
        Task task = TaskMapper.toEntity(request, category, assignee);
        Task saved = taskRepository.save(task);

        if (saved.getAssignee() != null) {
            notificationService.createNotification(
                saved.getAssignee(),
                "📋 Phân công công việc mới: " + saved.getTitle(),
                "Mức ưu tiên: " + saved.getPriority() + (saved.getDueDate() != null ? " | Hạn chót: " + saved.getDueDate() : ""),
                NotificationType.TASK_ASSIGNED,
                "/tasks"
            );
        }

        log.info("Created new task with ID [{}] and title [{}]", saved.getId(), saved.getTitle());
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);

        // Quy tắc nghiệp vụ 1: Task ở trạng thái DONE không được phép sửa nội dung
        if (task.getStatus() == TaskStatus.DONE) {
            log.warn("Attempted to edit completed task [{}]", id);
            throw new BusinessException(ErrorCode.TASK_COMPLETED_CANNOT_EDIT);
        }

        // Quy tắc nghiệp vụ 2: Khi cập nhật, nếu hạn chót THAY ĐỔI thì mới kiểm tra ngày mới >= hôm nay
        LocalDate newDueDate = request.getDueDate();
        if (newDueDate != null && !newDueDate.equals(task.getDueDate()) && newDueDate.isBefore(LocalDate.now())) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Hạn chót mới phải từ ngày hôm nay trở đi"
            );
        }

        Category category = findCategoryOrThrow(request.getCategoryId());
        User assignee = findAssigneeIfPresent(request.getAssigneeId());

        TaskMapper.updateEntity(task, request, category, assignee);
        log.info("Updated task with ID [{}]", task.getId());
        return TaskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = findTaskOrThrow(id);
        TaskStatus targetStatus = request.status();

        // Quy tắc nghiệp vụ State Machine: Kiểm tra tính hợp lệ của bước chuyển trạng thái
        if (!task.getStatus().canTransitionTo(targetStatus)) {
            log.warn("Invalid state transition for task [{}] from [{}] to [{}]", id, task.getStatus(), targetStatus);
            throw new InvalidStatusTransitionException(task.getStatus(), targetStatus);
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(targetStatus);
        log.info("Transitioned task [{}] status from [{}] to [{}]", id, oldStatus, targetStatus);

        if (task.getAssignee() != null) {
            notificationService.createNotification(
                task.getAssignee(),
                "🔄 Cập nhật trạng thái: " + task.getTitle(),
                "Trạng thái công việc đã chuyển sang " + targetStatus,
                NotificationType.STATUS_CHANGED,
                "/tasks"
            );
        }

        return TaskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
        log.info("Deleted task with ID [{}]", id);
    }

    @Override
    public List<KanbanColumnResponse> getKanbanBoard(Long categoryId, Long assigneeId) {
        // Lấy toàn bộ task thỏa mãn bộ lọc category/assignee
        Specification<Task> spec = TaskSpecification.build(
            null, null, null, categoryId, assigneeId, null, null
        );
        List<Task> allTasks = taskRepository.findAll(spec);

        // Gom nhóm các task theo từng trạng thái bằng Map
        Map<TaskStatus, List<TaskResponse>> groupedTasks = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            groupedTasks.put(status, new ArrayList<>());
        }

        for (Task task : allTasks) {
            groupedTasks.get(task.getStatus()).add(TaskMapper.toResponse(task));
        }

        // Đảm bảo trả về đúng 4 cột theo thứ tự vòng đời: TODO -> IN_PROGRESS -> REVIEW -> DONE
        return List.of(
            TaskMapper.toKanbanColumn(TaskStatus.TODO, groupedTasks.get(TaskStatus.TODO)),
            TaskMapper.toKanbanColumn(TaskStatus.IN_PROGRESS, groupedTasks.get(TaskStatus.IN_PROGRESS)),
            TaskMapper.toKanbanColumn(TaskStatus.REVIEW, groupedTasks.get(TaskStatus.REVIEW)),
            TaskMapper.toKanbanColumn(TaskStatus.DONE, groupedTasks.get(TaskStatus.DONE))
        );
    }

    // =========================================================================
    // CÁC HÀM TRUY VẤN THỐNG KÊ DASHBOARD
    // =========================================================================

    @Override
    public long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    public long countOverdueTasks() {
        return taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE);
    }

    @Override
    public long countUpcomingTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBetweenAndStatusNot(today, today.plusDays(7), TaskStatus.DONE);
    }

    // =========================================================================
    // PHƯƠNG THỨC TIỆN ÍCH RIÊNG (HELPER METHODS)
    // =========================================================================

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Công việc", id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Danh mục", categoryId));
    }

    private User findAssigneeIfPresent(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng", assigneeId));
    }
}
