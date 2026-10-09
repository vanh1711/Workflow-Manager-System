package com.example.taskmanager.common.validation;

import jakarta.validation.groups.Default;

/**
 * Marker Interface đại diện cho nhóm kiểm thực dữ liệu khi TẠO MỚI (Validation Group OnCreate).
 * Kế thừa {@link Default} để vẫn kích hoạt các ràng buộc kiểm thực mặc định không chỉ định nhóm.
 */
public interface OnCreate extends Default {
}
