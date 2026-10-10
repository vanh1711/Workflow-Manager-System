package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ErrorCode;
import com.example.taskmanager.exception.InvalidStatusTransitionException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử đơn vị (Unit Test) cho TaskServiceImpl sử dụng Mockito.
 * Kiểm thử độc lập logic nghiệp vụ mà không cần bật Spring Context hay kết nối Database thật,
 * tốc độ thực thi chỉ vài mili-giây.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.taskmanager.service.NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Category mockCategory;
    private User mockUser;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        mockCategory = Category.builder()
            .name("Core Banking")
            .colorHex("#4F46E5")
            .build();
        mockCategory.setId(1L);

        mockUser = User.builder()
            .username("dev_lead")
            .email("lead@bank.com")
            .fullName("Tech Lead")
            .role(Role.ADMIN)
            .build();
        mockUser.setId(10L);

        mockTask = Task.builder()
            .title("Xây dựng API thanh toán QR")
            .description("Tích hợp VietQR")
            .priority(Priority.HIGH)
            .status(TaskStatus.TODO)
            .dueDate(LocalDate.now().plusDays(5))
            .category(mockCategory)
            .assignee(mockUser)
            .build();
        mockTask.setId(100L);
    }

    @Test
    @DisplayName("Tạo Task thành công: Trạng thái luôn là TODO bất kể dữ liệu gửi lên")
    void createTask_success() {
        TaskRequest request = TaskRequest.builder()
            .title("Task mới")
            .description("Mô tả")
            .priority(Priority.HIGH)
            .categoryId(1L)
            .assigneeId(10L)
            .dueDate(LocalDate.now().plusDays(3))
            .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById(10L)).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals(TaskStatus.TODO, response.status(), "Task mới tạo phải luôn ở trạng thái TODO");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Tạo Task thất bại: Danh mục không tồn tại ném ResourceNotFoundException")
    void createTask_categoryNotFound_throwsException() {
        TaskRequest request = TaskRequest.builder()
            .title("Task")
            .categoryId(999L)
            .priority(Priority.MEDIUM)
            .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật Task thất bại: Task đã ở trạng thái DONE ném lỗi 409 TASK_COMPLETED_CANNOT_EDIT")
    void updateTask_whenStatusIsDone_throwsBusinessException() {
        mockTask.setStatus(TaskStatus.DONE);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskRequest request = TaskRequest.builder()
            .title("Cố tình sửa task đã hoàn thành")
            .categoryId(1L)
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.updateTask(100L, request));
        assertEquals(ErrorCode.TASK_COMPLETED_CANNOT_EDIT, ex.getErrorCode());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật Task thất bại: Đổi hạn chót sang ngày quá khứ bị chặn")
    void updateTask_whenDueDateChangedToPast_throwsException() {
        mockTask.setStatus(TaskStatus.IN_PROGRESS);
        mockTask.setDueDate(LocalDate.now().plusDays(2));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskRequest request = TaskRequest.builder()
            .title("Sửa hạn chót thành hôm qua")
            .dueDate(LocalDate.now().minusDays(1)) // Đổi sang quá khứ
            .categoryId(1L)
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.updateTask(100L, request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    @DisplayName("Đổi trạng thái hợp lệ: TODO sang IN_PROGRESS thành công")
    void changeStatus_validTransition_success() {
        mockTask.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS, null);

        TaskResponse response = taskService.changeStatus(100L, request);

        assertNotNull(response);
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
    }

    @Test
    @DisplayName("Di chuyển tự do: TODO nhảy thẳng sang DONE thành công")
    void changeStatus_freeTransition_todoDirectlyToDone() {
        mockTask.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.DONE, null);

        TaskResponse response = taskService.changeStatus(100L, request);

        assertNotNull(response);
        assertEquals(TaskStatus.DONE, response.status());
        assertEquals(TaskStatus.DONE, mockTask.getStatus());
    }

    @Test
    @DisplayName("Đổi sang chính trạng thái hiện tại (TODO sang TODO) ném lỗi 422 InvalidStatusTransitionException")
    void changeStatus_sameStatus_throwsException() {
        mockTask.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.TODO, null);

        assertThrows(InvalidStatusTransitionException.class, () -> taskService.changeStatus(100L, request));
        assertEquals(TaskStatus.TODO, mockTask.getStatus(), "Trạng thái không được thay đổi khi ném lỗi");
    }
}
