package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho thực thể Category / Project.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Kiểm tra tên danh mục đã tồn tại trong hệ thống chưa (chống trùng lặp khi tạo mới).
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên danh mục đã tồn tại cho một danh mục khác hay chưa (chống trùng lặp khi sửa).
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Lấy toàn bộ danh mục sắp xếp theo tên theo thứ tự alphabet tăng dần (dùng cho dropdown form).
     */
    List<Category> findAllByOrderByNameAsc();
}
