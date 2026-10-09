package com.example.taskmanager.common.validation;

import jakarta.validation.groups.Default;

/**
 * Marker Interface đại diện cho nhóm kiểm thực dữ liệu khi CẬP NHẬT (Validation Group OnUpdate).
 * Dùng để phân tách các quy tắc chỉ áp dụng khi cập nhật (ví dụ: cho phép sửa task đã quá hạn
 * mà không bị ràng buộc @FutureOrPresent chặn).
 */
public interface OnUpdate extends Default {
}
