package com.example.taskmanager.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Định nghĩa vòng đời trạng thái của công việc (Task Lifecycle State Machine).
 * Lớp enum này đóng vai trò "Nguồn sự thật duy nhất" (Single Source of Truth) cho các quy tắc
 * chuyển đổi trạng thái (State Transition Rules), ngăn chặn việc nhảy cóc trạng thái phi lý
 * (ví dụ: từ TODO nhảy thẳng sang DONE).
 */
@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    TODO("Cần làm", "bg-secondary-subtle text-secondary border border-secondary-subtle"),
    IN_PROGRESS("Đang thực hiện", "bg-primary-subtle text-primary border border-primary-subtle"),
    REVIEW("Đang duyệt", "bg-info-subtle text-info-emphasis border border-info-subtle"),
    DONE("Hoàn thành", "bg-success-subtle text-success border border-success-subtle");

    private final String label;
    private final String badgeClass;

    /**
     * Trả về danh sách các trạng thái khác mà trạng thái hiện tại được phép chuyển sang.
     * Cho phép di chuyển tự do giữa các trạng thái khác nhau (trừ chính nó).
     */
    public Set<TaskStatus> allowedNextStatuses() {
        Set<TaskStatus> all = EnumSet.allOf(TaskStatus.class);
        all.remove(this);
        return Collections.unmodifiableSet(all);
    }

    /**
     * Kiểm tra xem việc chuyển từ trạng thái hiện tại sang {@code target} có hợp lệ hay không.
     * Cho phép di chuyển tự do sang bất kỳ trạng thái nào khác (khác null và khác chính nó).
     *
     * @param target Trạng thái đích muốn chuyển sang
     * @return {@code true} nếu hợp lệ, ngược lại {@code false}
     */
    public boolean canTransitionTo(TaskStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return true;
    }
}
