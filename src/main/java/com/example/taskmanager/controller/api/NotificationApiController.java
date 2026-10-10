package com.example.taskmanager.controller.api;

import com.example.taskmanager.common.ApiResponse;
import com.example.taskmanager.dto.response.NotificationResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.CustomUserDetails;
import com.example.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách thông báo mới nhất kèm số lượng chưa đọc của người dùng hiện tại.
     * Đồng thời tự động đồng bộ cảnh báo Deadline/Quá hạn cho các công việc được giao.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("unreadCount", 0);
            emptyData.put("notifications", Collections.emptyList());
            return ResponseEntity.ok(ApiResponse.ok(emptyData, "Chưa đăng nhập"));
        }

        // Tự động kiểm tra và tạo cảnh báo Deadline/Quá hạn nếu có
        userRepository.findById(userDetails.getId()).ifPresent(notificationService::syncDeadlineNotifications);

        List<NotificationResponse> list = notificationService.getRecentNotifications(userDetails.getId(), 15);
        long unreadCount = notificationService.getUnreadCount(userDetails.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", unreadCount);
        data.put("notifications", list);

        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy danh sách thông báo thành công"));
    }

    /**
     * Đánh dấu 1 thông báo cụ thể là đã đọc.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            notificationService.markAsRead(id, userDetails.getId());
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã đánh dấu đã đọc"));
    }

    /**
     * Đánh dấu tất cả thông báo của người dùng là đã đọc.
     */
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails != null) {
            notificationService.markAllAsRead(userDetails.getId());
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã đánh dấu tất cả thông báo là đã đọc"));
    }
}
