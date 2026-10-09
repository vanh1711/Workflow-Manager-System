package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.ChangePasswordRequest;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.security.CustomUserDetails;
import com.example.taskmanager.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các chức năng liên quan đến Hồ sơ người dùng và Đổi mật khẩu cá nhân.
 */
@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileViewController {

    private final UserService userService;

    /**
     * Hiển thị giao diện Đổi mật khẩu cá nhân.
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        if (!model.containsAttribute("changePasswordRequest")) {
            model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        }
        model.addAttribute("activeNav", "profile");
        return "profile/change-password";
    }

    /**
     * Xử lý yêu cầu đổi mật khẩu cá nhân.
     */
    @PostMapping("/change-password")
    public String processChangePassword(
        @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
        BindingResult bindingResult,
        Authentication authentication,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Xác nhận mật khẩu mới không khớp");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "profile");
            return "profile/change-password";
        }

        Long currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            currentUserId = userDetails.getId();
        } else if (session != null && session.getAttribute("currentUserId") instanceof Long uid) {
            currentUserId = uid;
        }

        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
            return "redirect:/login";
        }

        try {
            userService.changePassword(currentUserId, request.getCurrentPassword(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Mật khẩu mới đã được cập nhật.");
            return "redirect:/profile/change-password";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeNav", "profile");
            return "profile/change-password";
        }
    }
}
