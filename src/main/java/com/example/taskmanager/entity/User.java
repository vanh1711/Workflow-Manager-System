package com.example.taskmanager.entity;

import com.example.taskmanager.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/**
 * Thực thể đại diện cho người dùng trong hệ thống (User Entity).
 * Lưu ý thiết kế kiến trúc:
 * - Không sử dụng {@code @Data} của Lombok để tránh sinh tự động {@code equals}, {@code hashCode}
 *   và {@code toString} gây xung đột proxy Hibernate hoặc lỗi lặp vô tận (infinite recursion).
 * - Sử dụng {@code EnumType.STRING} để lưu tên vai trò thay vì chỉ số số nguyên (ORDINAL).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "password", nullable = true, length = 255)
    private String password;

    @Enumerated(EnumType.STRING) // Lưu trữ dạng chuỗi (ADMIN/MEMBER) trong DB, không dùng số nguyên ORDINAL
    @Column(name = "role", nullable = false, length = 20)
    private Role role;
}
