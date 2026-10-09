package com.example.taskmanager.service;

import com.example.taskmanager.entity.Category;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ErrorCode;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử đơn vị cho CategoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Xóa danh mục thất bại: Danh mục đang có Task liên kết ném lỗi 409 CATEGORY_IN_USE")
    void deleteCategory_whenCategoryInUse_throwsBusinessException() {
        Category category = Category.builder().name("Dự án VIP").build();
        category.setId(5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(taskRepository.existsByCategoryId(5L)).thenReturn(true); // Đang có task

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.deleteCategory(5L));
        assertEquals(ErrorCode.CATEGORY_IN_USE, ex.getErrorCode());

        verify(categoryRepository, never()).delete(any());
    }
}
