package com.example.taskmanager.dto.request;

import com.example.taskmanager.common.validation.OnCreate;
import com.example.taskmanager.enums.Priority;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu tạo mới hoặc cập nhật công việc (dùng chung cho cả Form Thymeleaf và REST API).
 * Thiết kế dưới dạng POJO với Getter/Setter để Spring MVC có thể bind dữ liệu từ form x-www-form-urlencoded.
 *
 * Ứng dụng Bean Validation Groups:
 * - Khi Tạo Mới (OnCreate): Bắt buộc dueDate phải có (@NotNull) và phải từ hôm nay trở đi (@FutureOrPresent).
 * - Khi Cập Nhật (OnUpdate): Không ép @FutureOrPresent để người dùng vẫn có thể cập nhật nội dung các task đã quá hạn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @NotNull(groups = OnCreate.class, message = "Hạn chót không được để trống")
    @FutureOrPresent(groups = OnCreate.class, message = "Hạn chót phải từ hôm nay trở đi")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    private Priority priority;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private Long assigneeId;

    /**
     * Số version phục vụ Optimistic Locking khi cập nhật.
     */
    private Long version;
}
