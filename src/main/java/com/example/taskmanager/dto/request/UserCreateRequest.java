package com.example.taskmanager.dto.request;

import com.example.taskmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO nhận thông tin cấp tài khoản mới từ Quản trị viên (Admin).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Mật khẩu ban đầu không được để trống")
    @Size(min = 6, message = "Mật khẩu ban đầu tối thiểu 6 ký tự")
    private String password;

    @NotNull(message = "Vai trò người dùng không được để trống")
    private Role role;
}
