package com.example.taskmanager.dto.response;

/**
 * DTO trả về thông tin Danh mục / Dự án.
 */
public record CategoryResponse(
    Long id,
    String name,
    String description,
    String colorHex,
    Long taskCount
) {
}
