package com.example.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Thực thể đại diện cho Danh mục phân loại hoặc Dự án (Category / Project Entity).
 * Thiết kế quan hệ một chiều:
 * - Không khai báo quan hệ hai chiều {@code @OneToMany} trỏ về danh sách Task.
 *   Điều này giúp tránh việc vô tình kích hoạt tải toàn bộ Task trong danh mục vào bộ nhớ
 *   khi truy vấn Category, đồng thời ngăn ngừa vòng lặp serialize khi chuyển đổi JSON.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "color_hex", length = 7)
    private String colorHex;
}
