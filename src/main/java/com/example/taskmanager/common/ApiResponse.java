package com.example.taskmanager.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cấu trúc đóng gói chuẩn cho toàn bộ phản hồi RESTful API (Standard API Response Wrapper).
 * Đảm bảo mọi kết quả trả về từ API đều có định dạng JSON nhất quán, bất kể thành công hay thất bại.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Map<String, String> errors;
    private final LocalDateTime timestamp;
    private final String path;

    /**
     * Tạo phản hồi thành công mặc định HTTP 200 OK kèm dữ liệu và thông điệp.
     */
    public static <T> ApiResponse<T> ok(T data, String message, String path) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .errors(null)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ok(data, message, null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Thao tác thành công", null);
    }

    /**
     * Tạo phản hồi thành công khi tạo mới tài nguyên HTTP 201 CREATED.
     */
    public static <T> ApiResponse<T> created(T data, String message, String path) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .errors(null)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return created(data, message, null);
    }

    /**
     * Tạo phản hồi thất bại khi có lỗi hoặc vi phạm validation.
     */
    public static <T> ApiResponse<T> error(String message, String path, Map<String, String> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    public static <T> ApiResponse<T> error(String message, String path) {
        return error(message, path, null);
    }
}
