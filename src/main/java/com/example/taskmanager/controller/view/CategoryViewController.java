package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.CategoryRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller điều hướng giao diện Web Thymeleaf cho Danh mục / Dự án (Category / Project).
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("activeNav", "categories");
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(authentication, session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới được phép tạo danh mục dự án!");
            return "redirect:/categories";
        }

        CategoryRequest form = new CategoryRequest();
        form.setColorHex("#4F46E5");

        model.addAttribute("activeNav", "categories");
        model.addAttribute("categoryRequest", form);
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    @PostMapping
    public String createCategory(
        @Valid @ModelAttribute("categoryRequest") CategoryRequest categoryRequest,
        BindingResult bindingResult,
        Model model,
        Authentication authentication,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (isMemberRole(authentication, session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới được phép tạo danh mục dự án!");
            return "redirect:/categories";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "categories");
            model.addAttribute("isEdit", false);
            return "categories/form";
        }

        categoryService.createCategory(categoryRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo mới danh mục thành công!");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(authentication, session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền chỉnh sửa danh mục dự án!");
            return "redirect:/categories";
        }

        CategoryResponse category = categoryService.getCategoryById(id);
        CategoryRequest form = CategoryRequest.builder()
            .name(category.name())
            .description(category.description())
            .colorHex(category.colorHex())
            .build();

        model.addAttribute("activeNav", "categories");
        model.addAttribute("categoryId", id);
        model.addAttribute("categoryRequest", form);
        model.addAttribute("isEdit", true);
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String updateCategory(
        @PathVariable Long id,
        @Valid @ModelAttribute("categoryRequest") CategoryRequest categoryRequest,
        BindingResult bindingResult,
        Model model,
        Authentication authentication,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (isMemberRole(authentication, session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền chỉnh sửa danh mục dự án!");
            return "redirect:/categories";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "categories");
            model.addAttribute("categoryId", id);
            model.addAttribute("isEdit", true);
            return "categories/form";
        }

        categoryService.updateCategory(id, categoryRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, Authentication authentication, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(authentication, session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền xóa danh mục dự án!");
            return "redirect:/categories";
        }

        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        return "redirect:/categories";
    }

    private boolean isMemberRole(Authentication authentication, HttpSession session) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MEMBER".equals(a.getAuthority()));
        }
        if (session != null) {
            Object userObj = session.getAttribute("currentUser");
            if (userObj instanceof UserSummaryResponse user) {
                return "MEMBER".equalsIgnoreCase(user.role());
            }
        }
        return false;
    }
}
