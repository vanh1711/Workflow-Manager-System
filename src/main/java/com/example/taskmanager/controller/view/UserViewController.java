package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.UserCreateRequest;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.service.UserService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller quản lý và cấp tài khoản nhân viên nội bộ (Chỉ dành riêng cho Admin).
 */
@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserService userService;

    /**
     * Hiển thị danh sách nhân sự nội bộ và modal cấp tài khoản mới.
     */
    @GetMapping
    public String listUsers(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập chức năng Quản trị thành viên!");
            return "redirect:/tasks/my-tasks";
        }

        List<UserSummaryResponse> users = userService.getAllUsers();
        model.addAttribute("activeNav", "users");
        model.addAttribute("usersList", users);
        model.addAttribute("roles", Role.values());

        if (!model.containsAttribute("userCreateRequest")) {
            model.addAttribute("userCreateRequest", new UserCreateRequest());
        }

        return "users/list";
    }

    /**
     * Xử lý Quản trị viên cấp tài khoản mới cho nhân viên.
     */
    @PostMapping
    public String createUser(
        @Valid @ModelAttribute("userCreateRequest") UserCreateRequest request,
        BindingResult bindingResult,
        Authentication authentication,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        if (!isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ Quản trị viên mới có quyền cấp tài khoản mới!");
            return "redirect:/tasks/my-tasks";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "users");
            model.addAttribute("usersList", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            model.addAttribute("showCreateModal", true);
            return "users/list";
        }

        try {
            UserSummaryResponse newUser = userService.createUser(request);
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã cấp tài khoản thành công cho: " + newUser.fullName() + " (" + newUser.username() + ") với vai trò " + newUser.roleLabel() + "!"
            );
            return "redirect:/users";
        } catch (BusinessException ex) {
            model.addAttribute("activeNav", "users");
            model.addAttribute("usersList", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("showCreateModal", true);
            return "users/list";
        }
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
