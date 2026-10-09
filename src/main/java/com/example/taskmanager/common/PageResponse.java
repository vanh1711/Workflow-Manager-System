package com.example.taskmanager.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Cấu trúc đóng gói dữ liệu phân trang chuẩn cho REST API (Page Response Wrapper).
 * Chuẩn hóa các trường metadata (page, size, totalElements, totalPages, first, last)
 * giúp Frontend dễ dàng xây dựng thanh điều hướng phân trang.
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    /**
     * Phương thức tiện ích chuyển đổi từ đối tượng {@link Page} của Spring Data sang PageResponse.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        if (page == null) {
            return new PageResponse<>(List.of(), 0, 0, 0L, 0, true, true);
        }
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
