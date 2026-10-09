package com.example.taskmanager.controller.api;

import com.example.taskmanager.common.ApiResponse;
import com.example.taskmanager.dto.request.CategoryRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * RESTful API Controller quản lý Danh mục / Dự án (Category / Project).
 * Cung cấp các endpoint chuẩn JSON theo prefix {@code /api/v1/categories}.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    /**
     * Lấy danh sách toàn bộ danh mục sắp xếp theo tên.
     * GET /api/v1/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(HttpServletRequest request) {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories, "Lấy danh sách danh mục thành công", request.getRequestURI()));
    }

    /**
     * Lấy chi tiết một danh mục theo ID.
     * GET /api/v1/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.ok(category, "Lấy chi tiết danh mục thành công", request.getRequestURI()));
    }

    /**
     * Tạo mới một danh mục.
     * POST /api/v1/categories
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
        @Valid @RequestBody CategoryRequest categoryRequest,
        HttpServletRequest request
    ) {
        CategoryResponse created = categoryService.createCategory(categoryRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location)
            .body(ApiResponse.created(created, "Tạo danh mục mới thành công", request.getRequestURI()));
    }

    /**
     * Cập nhật thông tin danh mục.
     * PUT /api/v1/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody CategoryRequest categoryRequest,
        HttpServletRequest request
    ) {
        CategoryResponse updated = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật danh mục thành công", request.getRequestURI()));
    }

    /**
     * Xóa danh mục theo ID.
     * DELETE /api/v1/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
