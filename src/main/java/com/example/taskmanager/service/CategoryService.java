package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.CategoryRequest;
import com.example.taskmanager.dto.response.CategoryResponse;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Danh mục / Dự án (Category / Project).
 */
public interface CategoryService {

    /**
     * Lấy toàn bộ danh mục sắp xếp theo thứ tự alphabet kèm số lượng task liên kết.
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Lấy chi tiết danh mục theo ID.
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Tạo mới một danh mục (kiểm tra trùng lặp tên).
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Cập nhật thông tin danh mục.
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Xóa danh mục.
     * Quy tắc nghiệp vụ: Chặn xóa nếu danh mục đang có công việc liên kết (ném HTTP 409).
     */
    void deleteCategory(Long id);
}
