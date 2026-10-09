package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.UserCreateRequest;
import com.example.taskmanager.dto.response.UserSummaryResponse;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Người dùng (User).
 */
public interface UserService {

    /**
     * Lấy danh sách toàn bộ người dùng trong hệ thống (dùng cho dropdown giao diện và phân công việc).
     */
    List<UserSummaryResponse> getAllUsers();

    /**
     * Lấy thông tin tóm tắt của một người dùng theo ID.
     */
    UserSummaryResponse getUserById(Long id);

    /**
     * Xác thực thông tin đăng nhập người dùng bằng username/email và mật khẩu thô.
     * @deprecated Hiện tại xác thực đã được chuyển sang Spring Security 6 FormLogin.
     */
    @Deprecated
    UserSummaryResponse authenticate(String usernameOrEmail, String rawPassword);

    /**
     * Quản trị viên cấp tài khoản mới cho nhân viên nội bộ (có mã hóa BCrypt).
     */
    UserSummaryResponse createUser(UserCreateRequest request);

    /**
     * Đổi mật khẩu cá nhân cho người dùng đang đăng nhập.
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
