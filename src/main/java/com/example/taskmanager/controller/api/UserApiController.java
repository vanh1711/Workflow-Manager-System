package com.example.taskmanager.controller.api;

import com.example.taskmanager.common.ApiResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RESTful API Controller quản lý Người dùng (User).
 * Cung cấp các endpoint chuẩn JSON theo prefix {@code /api/v1/users}.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    /**
     * Lấy danh sách toàn bộ người dùng trong hệ thống (dùng cho dropdown giao diện hoặc tích hợp ngoài).
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers(HttpServletRequest request) {
        List<UserSummaryResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users, "Lấy danh sách người dùng thành công", request.getRequestURI()));
    }

    /**
     * Lấy chi tiết thông tin tóm tắt người dùng theo ID.
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserById(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        UserSummaryResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user, "Lấy thông tin người dùng thành công", request.getRequestURI()));
    }
}
