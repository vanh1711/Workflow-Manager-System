package com.example.taskmanager.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý điều hướng thông minh sau khi đăng nhập thành công:
 * - Quản trị viên (ADMIN) -> Chuyển hướng tới Bảng điều khiển (/dashboard).
 * - Thành viên (MEMBER) -> Chuyển hướng thẳng tới không gian làm việc cá nhân (/tasks/my-tasks).
 */
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        log.info("Người dùng '{}' đăng nhập thành công vào hệ thống", authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);

        String contextPath = request.getContextPath();

        // Đồng bộ thông tin phiên làm việc vào HttpSession để tương thích tuyệt đối mọi tầng View
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            request.getSession().setAttribute("currentUserId", userDetails.getId());
            com.example.taskmanager.dto.response.UserSummaryResponse userSummary = new com.example.taskmanager.dto.response.UserSummaryResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getEmail(),
                userDetails.getRole().name(),
                userDetails.getRole().getLabel()
            );
            request.getSession().setAttribute("currentUser", userSummary);
        }

        if (isAdmin) {
            response.sendRedirect(contextPath + "/dashboard");
        } else {
            response.sendRedirect(contextPath + "/tasks/my-tasks");
        }
    }
}
