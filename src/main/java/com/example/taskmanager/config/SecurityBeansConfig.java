package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình các Bean bảo mật cho hệ thống.
 * Cung cấp {@link PasswordEncoder} sử dụng thuật toán băm một chiều BCrypt
 * chuẩn công nghiệp để mã hóa mật khẩu người dùng trước khi lưu trữ vào cơ sở dữ liệu.
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
