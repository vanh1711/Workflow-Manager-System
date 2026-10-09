package com.example.taskmanager.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Định nghĩa mức độ ưu tiên của công việc (Task Priority).
 * Đính kèm nhãn tiếng Việt và class màu CSS của Bootstrap 5 để hiển thị badge giao diện nhất quán.
 */
@Getter
@RequiredArgsConstructor
public enum Priority {
    LOW("Thấp", "bg-light text-secondary border border-secondary-subtle"),
    MEDIUM("Trung bình", "bg-warning-subtle text-warning-emphasis border border-warning-subtle"),
    HIGH("Cao", "bg-danger-subtle text-danger border border-danger-subtle");

    private final String label;
    private final String badgeClass;
}
