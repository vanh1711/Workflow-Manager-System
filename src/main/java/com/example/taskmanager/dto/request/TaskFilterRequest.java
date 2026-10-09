package com.example.taskmanager.dto.request;

import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO nhận các tiêu chí tìm kiếm, lọc và phân trang từ Query Parameters.
 * Tích hợp cơ chế Whitelist Sorting để ngăn chặn việc người dùng truyền tham số sắp xếp độc hại
 * hoặc không tồn tại trong Entity, tránh lỗi PropertyReferenceException và bảo mật cơ sở dữ liệu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterRequest {

    private String keyword;
    private TaskStatus status;
    private Priority priority;
    private Long categoryId;
    private Long assigneeId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueTo;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";

    /**
     * Danh sách các trường dữ liệu hợp lệ cho phép sắp xếp (Whitelist Sorting).
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "title", "dueDate", "priority", "status", "createdAt"
    );

    /**
     * Chuyển đổi các tham số phân trang & sắp xếp thành đối tượng {@link Pageable} an toàn.
     * Tự động giới hạn size tối đa 50 bản ghi/trang để chống quá tải bộ nhớ.
     *
     * @throws IllegalArgumentException nếu field sắp xếp không nằm trong Whitelist
     */
    public Pageable toPageable() {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? Math.min(size, 50) : 10;

        String safeSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : "createdAt";
        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new IllegalArgumentException(
                "Trường sắp xếp không hợp lệ: '" + safeSortBy + "'. Các trường được phép: " + ALLOWED_SORT_FIELDS
            );
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, safeSortBy));
    }

    /**
     * Kiểm tra tính hợp lệ của khoảng thời gian hạn chót.
     *
     * @return true nếu dueFrom <= dueTo hoặc ít nhất 1 trong 2 bằng null
     */
    public boolean isValidDateRange() {
        if (dueFrom != null && dueTo != null) {
            return !dueFrom.isAfter(dueTo);
        }
        return true;
    }
}
