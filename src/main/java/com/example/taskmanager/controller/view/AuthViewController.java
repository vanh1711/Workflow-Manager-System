package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller điều hướng giao diện Đăng nhập (Login).
 * Tích hợp chặt chẽ với Spring Security 6 FormLogin:
 * - Hiển thị form đăng nhập với dữ liệu mẫu nội bộ.
 * - Bắt các cờ trạng thái '?error' (đăng nhập thất bại) và '?logout' (đăng xuất thành công).
 * - Tự động chuyển hướng nếu người dùng đã có phiên xác thực hợp lệ.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    /**
     * Hiển thị màn hình Đăng nhập (Login).
     */
    @GetMapping("/login")
    public String showLoginForm(
        @RequestParam(value = "error", required = false) String error,
        @RequestParam(value = "logout", required = false) String logout,
        Authentication authentication,
        Model model
    ) {
        // Nếu người dùng đã đăng nhập, tự động chuyển hướng theo vai trò
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
            return isAdmin ? "redirect:/dashboard" : "redirect:/tasks/my-tasks";
        }

        // Thông báo lỗi từ Spring Security FormLogin
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
        }

        // Thông báo sau khi đăng xuất thành công
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất khỏi hệ thống an toàn!");
        }

        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }

        return "auth/login";
    }
}
