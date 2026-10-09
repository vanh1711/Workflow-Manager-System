package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.security.CustomUserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * Cung cấp thông tin phiên người dùng hiện tại (currentUser, currentUserId),
 * danh sách tài khoản (allUsersList) và danh mục dự án (allCategoriesList)
 * cho toàn bộ các View Thymeleaf trực tiếp từ Spring Security Authentication.
 */
@ControllerAdvice(basePackages = "com.example.taskmanager.controller.view")
public class GlobalUserAdvice {

    private final UserService userService;
    private final CategoryService categoryService;

    public GlobalUserAdvice(
            ObjectProvider<UserService> userServiceProvider,
            ObjectProvider<CategoryService> categoryServiceProvider
    ) {
        this.userService = userServiceProvider.getIfAvailable();
        this.categoryService = categoryServiceProvider.getIfAvailable();
    }

    @ModelAttribute("allUsersList")
    public List<UserSummaryResponse> allUsers() {
        if (userService == null) {
            return Collections.emptyList();
        }
        return userService.getAllUsers();
    }

    @ModelAttribute("allCategoriesList")
    public List<CategoryResponse> allCategories() {
        if (categoryService == null) {
            return Collections.emptyList();
        }
        return categoryService.getAllCategories();
    }

    @ModelAttribute("currentUser")
    public UserSummaryResponse currentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return new UserSummaryResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFullName(),
                    userDetails.getEmail(),
                    userDetails.getRole().name(),
                    userDetails.getRole() == Role.ADMIN ? "Quản trị viên" : "Thành viên"
                );
            }
        }
        return null;
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        }
        return null;
    }
}
