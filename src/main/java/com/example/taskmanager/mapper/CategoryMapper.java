package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.request.CategoryRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.entity.Category;

/**
 * Lớp ánh xạ thủ công (Manual Mapper) cho đối tượng Category.
 * Lý do thiết kế kiến trúc:
 * - Không sử dụng MapStruct để giữ mã nguồn tường minh, dễ debug từng dòng,
 *   không phụ thuộc vào code gen trong compile-time và không gây xung đột với Lombok.
 */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    /**
     * Chuyển đổi từ Entity Category sang CategoryResponse DTO.
     */
    public static CategoryResponse toResponse(Category category) {
        return toResponse(category, null);
    }

    /**
     * Chuyển đổi từ Entity Category sang CategoryResponse DTO kèm theo số lượng task liên kết.
     */
    public static CategoryResponse toResponse(Category category, Long taskCount) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getColorHex(),
            taskCount
        );
    }

    /**
     * Chuyển đổi từ Request DTO sang Entity Category mới.
     */
    public static Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        return Category.builder()
            .name(request.getName().trim())
            .description(request.getDescription())
            .colorHex(request.getColorHex())
            .build();
    }

    /**
     * Cập nhật thông tin từ Request DTO vào Entity Category đang tồn tại.
     */
    public static void updateEntity(Category category, CategoryRequest request) {
        if (category == null || request == null) {
            return;
        }
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setColorHex(request.getColorHex());
    }
}
