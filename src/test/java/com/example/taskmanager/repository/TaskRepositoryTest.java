package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Category;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.repository.spec.TaskSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp tầng Repository và Dynamic Specification.
 * Sử dụng {@code @Transactional} để tự động rollback dữ liệu sau mỗi bài kiểm thử,
 * giữ sạch môi trường database.
 */
@SpringBootTest
@TestPropertySource(properties = "app.seeder.enabled=false")
@Transactional // Rollback toàn bộ dữ liệu test sau khi hoàn thành mỗi phương thức
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.taskmanager.repository.NotificationRepository notificationRepository;

    private User sampleUser;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAllInBatch();
        taskRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        sampleUser = userRepository.save(User.builder()
            .username("vanh_dev")
            .email("vanh@example.com")
            .fullName("V Anh")
            .role(Role.ADMIN)
            .build());

        sampleCategory = categoryRepository.save(Category.builder()
            .name("Core Banking Migration")
            .description("Dự án chuyển đổi hệ thống ngân hàng")
            .colorHex("#4F46E5")
            .build());
    }

    @Test
    @DisplayName("Kiểm tra Specification lọc theo từ khóa và trạng thái")
    void testFilterByKeywordAndStatus() {
        taskRepository.save(Task.builder()
            .title("Xây dựng module thanh toán")
            .description("Tích hợp cổng Napas")
            .priority(Priority.HIGH)
            .status(TaskStatus.TODO)
            .dueDate(LocalDate.now().plusDays(3))
            .category(sampleCategory)
            .assignee(sampleUser)
            .build());

        taskRepository.save(Task.builder()
            .title("Viết tài liệu API")
            .description("Mô tả endpoint chuyển khoản")
            .priority(Priority.LOW)
            .status(TaskStatus.DONE)
            .dueDate(LocalDate.now().plusDays(1))
            .category(sampleCategory)
            .assignee(sampleUser)
            .build());

        Specification<Task> spec = TaskSpecification.build(
            "thanh toán", TaskStatus.TODO, null, null, null, null, null
        );

        Page<Task> result = taskRepository.findAll(spec, PageRequest.of(0, 10, Sort.by("createdAt").descending()));

        assertEquals(1, result.getTotalElements());
        assertEquals("Xây dựng module thanh toán", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Kiểm tra truy vấn thống kê Dashboard: đếm quá hạn và sắp đến hạn")
    void testDashboardCountQueries() {
        LocalDate today = LocalDate.now();

        // Task 1: Quá hạn 2 ngày và chưa hoàn thành
        taskRepository.save(Task.builder()
            .title("Task quá hạn")
            .priority(Priority.HIGH)
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(today.minusDays(2))
            .category(sampleCategory)
            .build());

        // Task 2: Quá hạn nhưng ĐÃ hoàn thành (không tính vào quá hạn)
        taskRepository.save(Task.builder()
            .title("Task quá hạn nhưng đã xong")
            .priority(Priority.LOW)
            .status(TaskStatus.DONE)
            .dueDate(today.minusDays(5))
            .category(sampleCategory)
            .build());

        // Task 3: Sắp đến hạn trong 3 ngày tới
        taskRepository.save(Task.builder()
            .title("Task sắp đến hạn")
            .priority(Priority.MEDIUM)
            .status(TaskStatus.TODO)
            .dueDate(today.plusDays(3))
            .category(sampleCategory)
            .build());

        long overdueCount = taskRepository.countByDueDateBeforeAndStatusNot(today, TaskStatus.DONE);
        assertEquals(1, overdueCount);

        long upcomingCount = taskRepository.countByDueDateBetweenAndStatusNot(today, today.plusDays(7), TaskStatus.DONE);
        assertEquals(1, upcomingCount);

        long todoCount = taskRepository.countByStatus(TaskStatus.TODO);
        assertEquals(1, todoCount);
    }

    @Test
    @DisplayName("Kiểm tra existsByCategoryId để bảo vệ xóa danh mục")
    void testExistsByCategoryId() {
        taskRepository.save(Task.builder()
            .title("Task trong Category")
            .priority(Priority.MEDIUM)
            .status(TaskStatus.TODO)
            .category(sampleCategory)
            .build());

        assertTrue(taskRepository.existsByCategoryId(sampleCategory.getId()));
        assertFalse(taskRepository.existsByCategoryId(99999L));
    }
}
