package com.example.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Lớp thực thể cơ sở định nghĩa các trường dữ liệu chung cho toàn bộ các thực thể trong hệ thống.
 * {@code @MappedSuperclass} giúp các lớp con kế thừa các định nghĩa cột mà không tạo bảng riêng trong DB.
 * Sử dụng {@code @Version} để triển khai Optimistic Locking (Khóa lạc quan), ngăn ngừa xung đột dữ liệu
 * khi nhiều người dùng cùng thao tác cập nhật đồng thời.
 */
@Getter
@Setter
@MappedSuperclass // Không sinh bảng riêng, chuyển giao các trường ánh xạ xuống các Entity con
@EntityListeners(AuditingEntityListener.class) // Lắng nghe sự kiện để tự động gán createdAt, updatedAt
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version // Khóa lạc quan: Tự động tăng version mỗi lần UPDATE, phát hiện Lost Update
    @Column(name = "version")
    private Long version;
}
