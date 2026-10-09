package com.example.taskmanager.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý khi người dùng đã đăng nhập nhưng cố tình truy cập vào tài nguyên không đủ thẩm quyền (403 Forbidden).
 * Tự động chuyển hướng về không gian an toàn (/tasks/my-tasks) kèm thông báo từ chối quyền.
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        log.warn("Truy cập bị từ chối tới URL: {} - Lý do: {}", request.getRequestURI(), accessDeniedException.getMessage());
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/tasks/my-tasks?accessDenied=true");
    }
}
