package com.example.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Cấu hình kích hoạt cơ chế JPA Auditing của Spring Data JPA.
 * Tách riêng lớp cấu hình này thay vì đặt trực tiếp lên {@code @SpringBootApplication}
 * giúp các bài kiểm thử cắt lớp Web (ví dụ {@code @WebMvcTest}) không bị lỗi khi nạp context
 * do không yêu cầu EntityManagerFactory hoặc AuditorAware.
 */
@Configuration
@EnableJpaAuditing // Tự động điền ngày giờ tạo/sửa (@CreatedDate, @LastModifiedDate) vào Entity
public class JpaAuditingConfig {
}
