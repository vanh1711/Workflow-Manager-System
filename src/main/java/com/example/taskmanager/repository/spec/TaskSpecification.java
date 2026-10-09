package com.example.taskmanager.repository.spec;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Xây dựng các điều kiện truy vấn động (Dynamic Predicates) cho bảng Task bằng JPA Criteria API.
 * Thiết kế theo mẫu Specification Pattern của Eric Evans:
 * - Mỗi điều kiện lọc là một hàm độc lập, trả về {@link Specification}.
 * - Các điều kiện có thể dễ dàng ghép nối bằng toán tử logic {@code and()} hoặc {@code or()}.
 * - Tự động bỏ qua các điều kiện nếu giá trị đầu vào là null hoặc chuỗi rỗng.
 */
public final class TaskSpecification {

    private TaskSpecification() {
        // Private constructor để ngăn chặn việc khởi tạo tiện ích tĩnh
    }

    /**
     * Tìm kiếm gần đúng theo từ khóa (không phân biệt chữ hoa, chữ thường)
     * trong cả Tiêu đề (title) hoặc Mô tả (description).
     */
    public static Specification<Task> titleOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // Điều kiện luôn đúng (1=1), bỏ qua bộ lọc
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Lọc chính xác theo trạng thái công việc (status).
     */
    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Lọc chính xác theo mức độ ưu tiên (priority).
     */
    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) -> priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
    }

    /**
     * Lọc theo ID danh mục (category_id).
     */
    public static Specification<Task> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Lọc theo ID người được giao việc (user_id).
     */
    public static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null ? cb.conjunction() : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    /**
     * Lọc hạn chót từ ngày (dueDate >= dueFrom).
     */
    public static Specification<Task> dueFrom(LocalDate dueFrom) {
        return (root, query, cb) -> dueFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("dueDate"), dueFrom);
    }

    /**
     * Lọc hạn chót đến ngày (dueDate <= dueTo).
     */
    public static Specification<Task> dueTo(LocalDate dueTo) {
        return (root, query, cb) -> dueTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("dueDate"), dueTo);
    }

    /**
     * Tổng hợp toàn bộ các tiêu chí lọc đa chiều thành một đối tượng Specification duy nhất.
     */
    public static Specification<Task> build(
        String keyword,
        TaskStatus status,
        Priority priority,
        Long categoryId,
        Long assigneeId,
        LocalDate dueFrom,
        LocalDate dueTo
    ) {
        return Specification.where(titleOrDescriptionContains(keyword))
            .and(hasStatus(status))
            .and(hasPriority(priority))
            .and(hasCategoryId(categoryId))
            .and(hasAssigneeId(assigneeId))
            .and(dueFrom(dueFrom))
            .and(dueTo(dueTo));
    }

    /**
     * Tiện ích chuyển đổi trực tiếp từ đối tượng DTO {@link com.example.taskmanager.dto.request.TaskFilterRequest}.
     */
    public static Specification<Task> build(com.example.taskmanager.dto.request.TaskFilterRequest filter) {
        if (filter == null) {
            return Specification.where(null);
        }
        return build(
            filter.getKeyword(),
            filter.getStatus(),
            filter.getPriority(),
            filter.getCategoryId(),
            filter.getAssigneeId(),
            filter.getDueFrom(),
            filter.getDueTo()
        );
    }
}
