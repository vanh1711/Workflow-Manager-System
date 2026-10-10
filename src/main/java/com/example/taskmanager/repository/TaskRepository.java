package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Tầng truy xuất dữ liệu trung tâm cho thực thể Task.
 * Kế thừa song song:
 * - {@link JpaRepository}: Cung cấp các thao tác CRUD và phân trang cơ bản.
 * - {@link JpaSpecificationExecutor}: Cung cấp khả năng truy vấn động qua {@link Specification}.
 *
 * Chiến lược chống N+1 Queries:
 * - Sử dụng annotation {@code @EntityGraph(attributePaths = {"assignee", "category"})} trên các hàm tìm kiếm.
 *   Hibernate sẽ tự động chuyển đổi sang câu lệnh LEFT JOIN FETCH trên 1 truy vấn duy nhất thay vì
 *   bắn thêm hàng chục câu lệnh phụ để lấy assignee và category cho từng task.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Tìm kiếm chi tiết Task theo ID, nạp sẵn thông tin assignee và category tránh N+1.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"assignee", "category"}) // Nạp trước quan hệ LAZY bằng 1 câu lệnh JOIN
    Optional<Task> findById(@NonNull Long id);

    /**
     * Tìm kiếm phân trang kết hợp bộ lọc động Specification, nạp sẵn assignee và category.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"assignee", "category"})
    Page<Task> findAll(@Nullable Specification<Task> spec, @NonNull Pageable pageable);

    /**
     * Tìm kiếm danh sách không phân trang theo Specification (dùng khi lọc trên bảng Kanban).
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"assignee", "category"})
    List<Task> findAll(@Nullable Specification<Task> spec);

    /**
     * Lấy danh sách task theo trạng thái, sắp xếp mới nhất lên đầu, nạp sẵn quan hệ.
     */
    @EntityGraph(attributePaths = {"assignee", "category"})
    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    /**
     * Kiểm tra xem Danh mục có đang chứa công việc nào không.
     * Quy tắc nghiệp vụ: Ngăn chặn xóa Danh mục nếu đang có công việc liên kết.
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Đếm số lượng công việc thuộc về một Danh mục / Dự án cụ thể.
     */
    long countByCategoryId(Long categoryId);

    // =========================================================================
    // CÁC TRUY VẤN THỐNG KÊ CHO DASHBOARD
    // =========================================================================

    /**
     * Đếm tổng số task theo một trạng thái cụ thể.
     */
    long countByStatus(TaskStatus status);

    /**
     * Đếm số task đã quá hạn (dueDate < ngày hiện tại) và chưa hoàn thành (status != DONE).
     *
     * @param today  Ngày hiện tại
     * @param status Trạng thái loại trừ (thường là DONE)
     */
    long countByDueDateBeforeAndStatusNot(LocalDate today, TaskStatus status);

    /**
     * Đếm số task sắp đến hạn trong một khoảng ngày (ví dụ từ hôm nay đến 7 ngày tới) và chưa hoàn thành.
     *
     * @param start  Ngày bắt đầu (hôm nay)
     * @param end    Ngày kết thúc (hôm nay + 7 ngày)
     * @param status Trạng thái loại trừ (DONE)
     */
    long countByDueDateBetweenAndStatusNot(LocalDate start, LocalDate end, TaskStatus status);

    /**
     * Lấy danh sách công việc chưa hoàn thành của một người dùng, phục vụ cảnh báo hạn chót và thông báo.
     */
    List<Task> findByAssigneeIdAndStatusNotOrderByDueDateAsc(Long assigneeId, TaskStatus status);
}
