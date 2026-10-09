package com.example.taskmanager.exception;

import com.example.taskmanager.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tầng xử lý ngoại lệ tập trung dành riêng cho RESTful API (REST API Global Exception Handler).
 * Giới hạn phạm vi qua {@code basePackages = "com.example.taskmanager.controller.api"}.
 *
 * Nguyên tắc bảo mật & chuẩn mực ngân hàng:
 * - Trả về định dạng {@link ApiResponse} thống nhất 100% với HTTP Status Code chuẩn.
 * - Tuyệt đối KHÔNG ĐƯỢC để lộ StackTrace hoặc thông tin schema DB cho client trong phản hồi 500.
 * - Ghi log chi tiết bằng SLF4J với mức độ ERROR để phục vụ việc tra cứu và điều tra sự cố.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.example.taskmanager.controller.api")
public class ApiExceptionHandler {

    /**
     * Bắt lỗi vi phạm Bean Validation trên @RequestBody (@Valid / @Validated).
     * Trích xuất chính xác tên từng trường vi phạm kèm thông điệp lỗi tiếng Việt.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed for request [{}]: {}", request.getRequestURI(), errors);

        ApiResponse<Void> response = ApiResponse.error(
            "Dữ liệu không hợp lệ",
            request.getRequestURI(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi vi phạm ràng buộc trên URL Path Variable hoặc Request Param (@Validated mức class).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
        ConstraintViolationException ex, HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        ApiResponse<Void> response = ApiResponse.error(
            "Tham số yêu cầu không hợp lệ",
            request.getRequestURI(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi sai kiểu dữ liệu tham số (ví dụ: ID truyền chữ thay vì số).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request
    ) {
        String message = String.format("Tham số '%s' có giá trị '%s' không đúng kiểu dữ liệu yêu cầu", ex.getName(), ex.getValue());
        ApiResponse<Void> response = ApiResponse.error(message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi JSON payload không hợp lệ hoặc không parse được enum.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex, HttpServletRequest request
    ) {
        log.warn("Malformed JSON request [{}]: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("Định dạng dữ liệu JSON gửi lên không hợp lệ", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi tham số nghiệp vụ (ví dụ: Sort field không nằm trong Whitelist).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
        IllegalArgumentException ex, HttpServletRequest request
    ) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi không tìm thấy tài nguyên (HTTP 404 NOT FOUND).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
        ResourceNotFoundException ex, HttpServletRequest request
    ) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Bắt lỗi trùng lặp tài nguyên duy nhất (HTTP 409 CONFLICT).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
        DuplicateResourceException ex, HttpServletRequest request
    ) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Bắt lỗi xung đột phiên bản Optimistic Locking (@Version) khi có tranh chấp cập nhật đồng thời.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(
        ObjectOptimisticLockingFailureException ex, HttpServletRequest request
    ) {
        log.warn("Optimistic locking conflict on request [{}]: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getDefaultMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Bắt lỗi vi phạm quy tắc chuyển trạng thái Task State Machine (HTTP 422 UNPROCESSABLE ENTITY).
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStatusTransition(
        InvalidStatusTransitionException ex, HttpServletRequest request
    ) {
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Bắt các ngoại lệ nghiệp vụ cơ sở khác (BusinessException).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
        BusinessException ex, HttpServletRequest request
    ) {
        HttpStatus status = ex.getErrorCode() != null ? ex.getErrorCode().getHttpStatus() : HttpStatus.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Bắt toàn bộ các lỗi ngoại lệ chưa được dự trù (HTTP 500 INTERNAL SERVER ERROR).
     * Ghi log đầy đủ stacktrace để lập trình viên tra cứu, nhưng chỉ trả về thông điệp chung chung cho client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
        Exception ex, HttpServletRequest request
    ) {
        log.error("Unhandled internal server error occurred on path [{}]: ", request.getRequestURI(), ex);
        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
