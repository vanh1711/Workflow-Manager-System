package com.example.taskmanager.exception;

import com.example.taskmanager.common.ApiResponse;
import com.example.taskmanager.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho tầng xử lý ngoại lệ tập trung ApiExceptionHandler.
 * Đảm bảo các mã HTTP status và cấu trúc ApiResponse phản hồi chính xác.
 */
class ApiExceptionHandlerTest {

    private ApiExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ApiExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/tasks/99");
    }

    @Test
    @DisplayName("ResourceNotFoundException phải trả về HTTP 404 NOT FOUND")
    void handleResourceNotFound_shouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Task", 99L);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Không tìm thấy Task"));
        assertEquals("/api/v1/tasks/99", response.getBody().getPath());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("DuplicateResourceException phải trả về HTTP 409 CONFLICT")
    void handleDuplicateResource_shouldReturn409() {
        DuplicateResourceException ex = new DuplicateResourceException(
            ErrorCode.CATEGORY_NAME_DUPLICATE, "Tên danh mục đã tồn tại"
        );

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDuplicateResource(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Tên danh mục đã tồn tại", response.getBody().getMessage());
    }

    @Test
    @DisplayName("InvalidStatusTransitionException phải trả về HTTP 422 UNPROCESSABLE ENTITY")
    void handleInvalidStatusTransition_shouldReturn422() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
            TaskStatus.TODO, TaskStatus.DONE
        );

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleInvalidStatusTransition(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Không thể chuyển từ trạng thái 'Cần làm' sang 'Hoàn thành'"));
    }

    @Test
    @DisplayName("ObjectOptimisticLockingFailureException phải trả về HTTP 409 CONFLICT")
    void handleOptimisticLockingFailure_shouldReturn409() {
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException(
            "Task", 1L
        );

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleOptimisticLockingFailure(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getDefaultMessage(), response.getBody().getMessage());
    }

    @Test
    @DisplayName("Ngoại lệ không mong muốn Exception phải trả về HTTP 500 và thông điệp chung, không lộ stacktrace")
    void handleGlobalException_shouldReturn500WithoutLeakingDetails() {
        Exception ex = new RuntimeException("Lỗi rò rỉ database connection string hoặc secret key!");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("database connection string"));
    }
}
