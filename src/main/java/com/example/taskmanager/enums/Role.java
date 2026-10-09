package com.example.taskmanager.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Định nghĩa vai trò của người dùng trong hệ thống.
 * Được lưu trữ dưới dạng chuỗi (EnumType.STRING) trong cơ sở dữ liệu để đảm bảo tính tường minh.
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("Quản trị viên"),
    MEMBER("Thành viên");

    private final String label;
}
