package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.request.CategoryRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.entity.Category;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.DuplicateResourceException;
import com.example.taskmanager.exception.ErrorCode;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.CategoryMapper;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ Danh mục / Dự án.
 * Quản lý ranh giới giao dịch {@code @Transactional} và thực thi các quy tắc toàn vẹn dữ liệu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
            .map(cat -> CategoryMapper.toResponse(cat, taskRepository.countByCategoryId(cat.getId())))
            .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .map(cat -> CategoryMapper.toResponse(cat, taskRepository.countByCategoryId(cat.getId())))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional // Ghi đè: Phương thức ghi cần transaction đọc-ghi để lưu dữ liệu
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByName(trimmedName)) {
            throw new DuplicateResourceException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        Category category = CategoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        log.info("Created new category with ID [{}] and name [{}]", saved.getId(), saved.getName());
        return CategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        CategoryMapper.updateEntity(category, request);
        log.info("Updated category with ID [{}]", id);
        return CategoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        // Quy tắc toàn vẹn dữ liệu: Không cho phép xóa Danh mục đang có công việc liên kết
        if (taskRepository.existsByCategoryId(id)) {
            log.warn("Attempted to delete in-use category [{}] with active tasks", id);
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID [{}]", id);
    }
}
