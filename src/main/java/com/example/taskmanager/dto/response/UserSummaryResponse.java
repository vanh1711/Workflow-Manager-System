package com.example.taskmanager.dto.response;

/**
 * DTO trả về thông tin tóm tắt của người dùng (dùng cho dropdown giao diện và thông tin Assignee).
 * Sử dụng Java Record để đảm bảo tính bất biến (Immutable), cú pháp ngắn gọn và tự động sinh
 * constructor, getter, equals, hashCode, toString.
 */
public record UserSummaryResponse(
    Long id,
    String username,
    String fullName,
    String email,
    String role,
    String roleLabel
) {
}
