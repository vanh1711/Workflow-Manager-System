package com.example.taskmanager.enums;

/**
 * Phân loại các thông báo trong hệ thống TaskFlow:
 * - TASK_ASSIGNED: Được phân công công việc mới
 * - TASK_DUE_SOON: Cảnh báo sắp đến hạn hoàn thành (Deadline)
 * - TASK_OVERDUE: Cảnh báo công việc đã quá hạn
 * - STATUS_CHANGED: Trạng thái công việc được cập nhật
 * - SYSTEM: Thông báo từ hệ thống hoặc quản trị viên
 */
public enum NotificationType {
    TASK_ASSIGNED,
    TASK_DUE_SOON,
    TASK_OVERDUE,
    STATUS_CHANGED,
    SYSTEM
}
