package com.example.taskmanager.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị (Unit Test) cho máy trạng thái (State Machine) của TaskStatus.
 * Đảm bảo các quy tắc chuyển đổi trạng thái hoạt động chính xác 100% trước khi đưa vào Service.
 */
class TaskStatusTest {

    @Test
    @DisplayName("TODO được phép chuyển tự do sang IN_PROGRESS, REVIEW hoặc DONE")
    void todoStatus_shouldTransitionToAnyOtherStatus() {
        TaskStatus status = TaskStatus.TODO;

        assertTrue(status.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertTrue(status.canTransitionTo(TaskStatus.REVIEW));
        assertTrue(status.canTransitionTo(TaskStatus.DONE));
        assertFalse(status.canTransitionTo(TaskStatus.TODO));
        assertFalse(status.canTransitionTo(null));

        assertEquals(Set.of(TaskStatus.IN_PROGRESS, TaskStatus.REVIEW, TaskStatus.DONE), status.allowedNextStatuses());
    }

    @Test
    @DisplayName("IN_PROGRESS được phép chuyển tự do sang TODO, REVIEW hoặc DONE")
    void inProgressStatus_shouldTransitionToAnyOtherStatus() {
        TaskStatus status = TaskStatus.IN_PROGRESS;

        assertTrue(status.canTransitionTo(TaskStatus.TODO));
        assertTrue(status.canTransitionTo(TaskStatus.REVIEW));
        assertTrue(status.canTransitionTo(TaskStatus.DONE));
        assertFalse(status.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertFalse(status.canTransitionTo(null));

        assertEquals(Set.of(TaskStatus.TODO, TaskStatus.REVIEW, TaskStatus.DONE), status.allowedNextStatuses());
    }

    @Test
    @DisplayName("REVIEW được phép chuyển tự do sang TODO, IN_PROGRESS hoặc DONE")
    void reviewStatus_shouldTransitionToAnyOtherStatus() {
        TaskStatus status = TaskStatus.REVIEW;

        assertTrue(status.canTransitionTo(TaskStatus.TODO));
        assertTrue(status.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertTrue(status.canTransitionTo(TaskStatus.DONE));
        assertFalse(status.canTransitionTo(TaskStatus.REVIEW));
        assertFalse(status.canTransitionTo(null));

        assertEquals(Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE), status.allowedNextStatuses());
    }

    @Test
    @DisplayName("DONE được phép mở lại và chuyển tự do sang TODO, IN_PROGRESS hoặc REVIEW")
    void doneStatus_shouldTransitionToAnyOtherStatus() {
        TaskStatus status = TaskStatus.DONE;

        assertTrue(status.canTransitionTo(TaskStatus.TODO));
        assertTrue(status.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertTrue(status.canTransitionTo(TaskStatus.REVIEW));
        assertFalse(status.canTransitionTo(TaskStatus.DONE));
        assertFalse(status.canTransitionTo(null));

        assertEquals(Set.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.REVIEW), status.allowedNextStatuses());
    }
}
