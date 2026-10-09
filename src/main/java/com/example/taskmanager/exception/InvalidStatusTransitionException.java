package com.example.taskmanager.exception;

import com.example.taskmanager.enums.TaskStatus;
import lombok.Getter;

import java.util.stream.Collectors;

/**
 * Ngoại lệ ném ra khi vi phạm luật chuyển đổi trạng thái của Task State Machine (tương ứng HTTP 422 UNPROCESSABLE ENTITY).
 * Cung cấp thông báo lỗi rõ ràng giải thích trạng thái hiện tại, trạng thái đích và danh sách các trạng thái hợp lệ.
 */
@Getter
public class InvalidStatusTransitionException extends BusinessException {

    private final TaskStatus currentStatus;
    private final TaskStatus targetStatus;

    public InvalidStatusTransitionException(TaskStatus currentStatus, TaskStatus targetStatus) {
        super(
            ErrorCode.INVALID_STATUS_TRANSITION,
            buildMessage(currentStatus, targetStatus)
        );
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    private static String buildMessage(TaskStatus currentStatus, TaskStatus targetStatus) {
        String currentLabel = currentStatus != null ? currentStatus.getLabel() : "Không xác định";
        String targetLabel = targetStatus != null ? targetStatus.getLabel() : "Không xác định";

        String allowedList = currentStatus != null
            ? currentStatus.allowedNextStatuses().stream()
                .map(s -> s.name() + " (" + s.getLabel() + ")")
                .collect(Collectors.joining(", ", "[", "]"))
            : "[]";

        return String.format(
            "Không thể chuyển từ trạng thái '%s' sang '%s'. Các trạng thái tiếp theo hợp lệ: %s",
            currentLabel, targetLabel, allowedList
        );
    }
}
