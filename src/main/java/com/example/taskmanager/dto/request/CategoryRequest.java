package com.example.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO nhận yêu cầu tạo mới hoặc cập nhật Danh mục / Dự án.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @Pattern(
        regexp = "^#([A-Fa-f0-9]{6})$",
        message = "Mã màu phải có định dạng mã HEX gồm 6 ký tự (ví dụ: #4F46E5)"
    )
    private String colorHex;

    private Long version;
}
