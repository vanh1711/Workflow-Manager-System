package com.example.taskmanager.dto;

import com.example.taskmanager.common.validation.OnCreate;
import com.example.taskmanager.common.validation.OnUpdate;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.mapper.TaskMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho tầng DTO Validation Groups, Whitelist Sorting và Mapper thủ công.
 */
class TaskValidationAndMapperTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Validation OnCreate: Bắt buộc title, priority, categoryId và dueDate không được ở quá khứ")
    void testValidationOnCreate_invalidFields() {
        TaskRequest request = TaskRequest.builder()
            .title("") // Trống
            .priority(null) // Thiếu
            .categoryId(null) // Thiếu
            .dueDate(LocalDate.now().minusDays(1)) // Quá khứ
            .build();

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request, OnCreate.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dueDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("categoryId")));
    }

    @Test
    @DisplayName("Validation OnUpdate: Cho phép dueDate ở quá khứ (không bị chặn khi sửa nội dung task quá hạn)")
    void testValidationOnUpdate_allowsPastDueDate() {
        TaskRequest request = TaskRequest.builder()
            .title("Cập nhật task đã quá hạn")
            .priority(Priority.MEDIUM)
            .categoryId(1L)
            .dueDate(LocalDate.now().minusDays(2)) // Quá khứ
            .build();

        // Kiểm tra với nhóm OnUpdate
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request, OnUpdate.class);

        // dueDate không nằm trong OnUpdate nên không vi phạm
        assertTrue(violations.isEmpty(), "OnUpdate không được chặn dueDate ở quá khứ");
    }

    @Test
    @DisplayName("TaskFilterRequest: Whitelist sorting bảo vệ các field hợp lệ và chặn field lạ")
    void testWhitelistSorting() {
        TaskFilterRequest validFilter = TaskFilterRequest.builder()
            .page(0)
            .size(10)
            .sortBy("dueDate")
            .sortDirection("asc")
            .build();

        Pageable pageable = validFilter.toPageable();
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        // Field không nằm trong whitelist -> ném IllegalArgumentException
        TaskFilterRequest maliciousFilter = TaskFilterRequest.builder()
            .sortBy("password_hash")
            .build();

        assertThrows(IllegalArgumentException.class, maliciousFilter::toPageable);
    }

    @Test
    @DisplayName("TaskMapper: Tạo task mới luôn có status là TODO và tính đúng cờ overdue")
    void testTaskMapper() {
        Category category = Category.builder().name("Backend").colorHex("#4F46E5").build();
        User user = User.builder().username("dev1").fullName("Dev One").email("dev1@test.com").role(Role.MEMBER).build();

        TaskRequest request = TaskRequest.builder()
            .title("Task mới")
            .description("Mô tả")
            .priority(Priority.HIGH)
            .dueDate(LocalDate.now().plusDays(2))
            .build();

        Task task = TaskMapper.toEntity(request, category, user);
        assertNotNull(task);
        assertEquals(TaskStatus.TODO, task.getStatus(), "Task mới tạo phải luôn ở trạng thái TODO");

        // Kiểm tra tính toán cờ overdue
        task.setDueDate(LocalDate.now().minusDays(1)); // Quá hạn
        task.setStatus(TaskStatus.IN_PROGRESS);
        TaskResponse response = TaskMapper.toResponse(task);
        assertNotNull(response);
        assertTrue(response.overdue(), "Task quá hạn và chưa hoàn thành thì overdue phải là true");

        task.setStatus(TaskStatus.DONE); // Đã xong thì không tính là overdue
        TaskResponse responseDone = TaskMapper.toResponse(task);
        assertFalse(responseDone.overdue(), "Task đã DONE thì không tính là overdue");
    }
}
