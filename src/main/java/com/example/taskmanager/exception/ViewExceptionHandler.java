package com.example.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tầng xử lý ngoại lệ tập trung dành riêng cho giao diện Thymeleaf (MVC View Global Exception Handler).
 * Giới hạn phạm vi qua {@code basePackages = "com.example.taskmanager.controller.view"}.
 *
 * Chiến lược xử lý thân thiện với người dùng:
 * - Khi không tìm thấy tài nguyên: Điều hướng đến trang thông báo lỗi 404 thân thiện kèm nút quay về trang chủ.
 * - Khi có lỗi nghiệp vụ: Giữ chân người dùng, chuyển hướng về trang trước (hoặc danh sách) kèm flash message cảnh báo.
 * - Khi có lỗi hệ thống không mong muốn: Điều hướng đến trang 500 thông báo bảo trì, đồng thời log stacktrace ở server.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.example.taskmanager.controller.view")
public class ViewExceptionHandler {

    /**
     * Xử lý lỗi không tìm thấy tài nguyên trên giao diện web (trả về trang 404.html).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Web resource not found [{}]: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestedUri", request.getRequestURI());
        return "error/404";
    }

    /**
     * Xử lý lỗi nghiệp vụ trên giao diện web (chuyển hướng kèm flash message thông báo lỗi màu đỏ).
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
        BusinessException ex, HttpServletRequest request, RedirectAttributes redirectAttributes
    ) {
        log.warn("Business rule violation in web view [{}]: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/tasks";
    }

    /**
     * Xử lý lỗi xung đột cập nhật đồng thời (Optimistic Locking @Version) trên giao diện web.
     * Thông báo nhẹ nhàng và hướng dẫn người dùng tải lại trang thay vì văng lỗi 500.
     */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLockingFailure(
        org.springframework.orm.ObjectOptimisticLockingFailureException ex,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        log.warn("Optimistic locking conflict in web view [{}]: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute(
            "errorMessage",
            "Dữ liệu công việc vừa được cập nhật bởi thành viên khác. Vui lòng tải lại trang để xem dữ liệu mới nhất!"
        );

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/tasks";
    }

    /**
     * Xử lý lỗi ngoại lệ không mong muốn trên giao diện web (trả về trang 500.html).
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {
        log.error("Internal server error in web view [{}]: ", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
        return "error/500";
    }
}
