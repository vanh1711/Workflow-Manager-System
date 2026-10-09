package com.example.taskmanager.controller.view;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử tầng điều hướng giao diện Web Thymeleaf (TaskViewController).
 * Đảm bảo các mô hình Post/Redirect/Get (PRG), truyền flash attributes và binding lỗi diễn ra đúng thiết kế.
 */
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = TaskViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    private TaskResponse sampleTask;
    private UserSummaryResponse adminUser;

    @BeforeEach
    void setUp() {
        CategoryResponse cat = new CategoryResponse(1L, "Backend", "Phân hệ API", "#4F46E5", 0L);
        adminUser = new UserSummaryResponse(1L, "admin", "Nguyen Van An", "admin@company.com", "ADMIN", "Quản trị viên");
        UserSummaryResponse user = adminUser;

        sampleTask = new TaskResponse(
            1L,
            "Thiết kế kiến trúc",
            "Mô tả chi tiết công việc",
            LocalDate.now().plusDays(5),
            Priority.HIGH,
            Priority.HIGH.getLabel(),
            Priority.HIGH.getBadgeClass(),
            TaskStatus.TODO,
            TaskStatus.TODO.getLabel(),
            TaskStatus.TODO.getBadgeClass(),
            user,
            cat,
            LocalDateTime.now(),
            LocalDateTime.now(),
            0L,
            false
        );

        when(categoryService.getAllCategories()).thenReturn(List.of(cat));
        when(userService.getAllUsers()).thenReturn(List.of(user));
    }

    @Test
    @DisplayName("GET /tasks - Trả về view tasks/list và nạp danh sách phân trang")
    void listTasks_ShouldReturnListView() throws Exception {
        PageResponse<TaskResponse> page = new PageResponse<>(
            List.of(sampleTask), 0, 10, 1L, 1, true, true
        );

        when(taskService.getTasks(any(TaskFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/tasks").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/list"))
            .andExpect(model().attributeExists("taskPage", "activeNav", "categories", "users", "statuses", "priorities"));
    }

    @Test
    @DisplayName("GET /tasks/kanban - Trả về view tasks/kanban và dữ liệu 4 cột")
    void kanbanBoard_ShouldReturnKanbanView() throws Exception {
        List<KanbanColumnResponse> columns = List.of(
            TaskMapper.toKanbanColumn(TaskStatus.TODO, List.of(sampleTask)),
            TaskMapper.toKanbanColumn(TaskStatus.IN_PROGRESS, Collections.emptyList()),
            TaskMapper.toKanbanColumn(TaskStatus.REVIEW, Collections.emptyList()),
            TaskMapper.toKanbanColumn(TaskStatus.DONE, Collections.emptyList())
        );

        when(taskService.getKanbanBoard(any(), any())).thenReturn(columns);

        mockMvc.perform(get("/tasks/kanban").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/kanban"))
            .andExpect(model().attributeExists("columns", "activeNav"));
    }

    @Test
    @DisplayName("GET /tasks/my-tasks - Trả về view tasks/kanban với cấu hình My Task")
    void myTasks_ShouldReturnKanbanView() throws Exception {
        List<KanbanColumnResponse> columns = List.of(
            TaskMapper.toKanbanColumn(TaskStatus.TODO, List.of(sampleTask)),
            TaskMapper.toKanbanColumn(TaskStatus.IN_PROGRESS, Collections.emptyList()),
            TaskMapper.toKanbanColumn(TaskStatus.REVIEW, Collections.emptyList()),
            TaskMapper.toKanbanColumn(TaskStatus.DONE, Collections.emptyList())
        );

        when(taskService.getKanbanBoard(any(), any())).thenReturn(columns);

        mockMvc.perform(get("/tasks/my-tasks").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/kanban"))
            .andExpect(model().attributeExists("columns", "activeNav", "isMyTasksView"));
    }

    @Test
    @DisplayName("GET /tasks/calendar - Admin thấy toàn bộ công việc trong tháng")
    void calendar_Admin_ShouldShowAllTasks() throws Exception {
        PageResponse<TaskResponse> page = new PageResponse<>(
            List.of(sampleTask), 0, 50, 1L, 1, true, true
        );
        when(taskService.getTasks(any(TaskFilterRequest.class))).thenReturn(page);

        UserSummaryResponse adminUser = new UserSummaryResponse(1L, "admin", "Admin User", "admin@test.com", "ADMIN", "Quản trị viên");

        mockMvc.perform(get("/tasks/calendar")
                .sessionAttr("currentUser", adminUser)
                .sessionAttr("currentUserId", 1L))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/calendar"))
            .andExpect(model().attribute("isMember", false))
            .andExpect(model().attribute("totalMonthTasks", 1));

        org.mockito.ArgumentCaptor<TaskFilterRequest> filterCaptor = org.mockito.ArgumentCaptor.forClass(TaskFilterRequest.class);
        verify(taskService).getTasks(filterCaptor.capture());
        org.junit.jupiter.api.Assertions.assertNull(filterCaptor.getValue().getAssigneeId());
    }

    @Test
    @DisplayName("GET /tasks/calendar - Thành viên (Member) chỉ thấy công việc được giao cho chính mình")
    void calendar_Member_ShouldFilterByCurrentUserId() throws Exception {
        PageResponse<TaskResponse> page = new PageResponse<>(
            List.of(sampleTask), 0, 50, 1L, 1, true, true
        );
        when(taskService.getTasks(any(TaskFilterRequest.class))).thenReturn(page);

        UserSummaryResponse memberUser = new UserSummaryResponse(2L, "member", "Member User", "member@test.com", "MEMBER", "Nhân viên");

        mockMvc.perform(get("/tasks/calendar")
                .sessionAttr("currentUser", memberUser)
                .sessionAttr("currentUserId", 2L))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/calendar"))
            .andExpect(model().attribute("isMember", true))
            .andExpect(model().attribute("selectedAssigneeId", 2L));

        org.mockito.ArgumentCaptor<TaskFilterRequest> filterCaptor = org.mockito.ArgumentCaptor.forClass(TaskFilterRequest.class);
        verify(taskService).getTasks(filterCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(2L, filterCaptor.getValue().getAssigneeId());
    }

    @Test
    @DisplayName("GET /tasks/new - Trả về view tasks/form cho tạo mới")
    void showCreateForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/tasks/new").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/form"))
            .andExpect(model().attribute("isEdit", false))
            .andExpect(model().attributeExists("taskRequest"));
    }

    @Test
    @DisplayName("POST /tasks hợp lệ - Áp dụng mẫu PRG chuyển hướng về /tasks kèm flashMessage")
    void createTask_Valid_ShouldRedirect() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks").sessionAttr("currentUser", adminUser)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Task Mới Hợp Lệ")
                .param("description", "Mô tả hợp lệ")
                .param("dueDate", LocalDate.now().plusDays(2).toString())
                .param("priority", "HIGH")
                .param("categoryId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(taskService).createTask(any(TaskRequest.class));
    }

    @Test
    @DisplayName("POST /tasks không hợp lệ - Trả lại view tasks/form mà không redirect")
    void createTask_Invalid_ShouldReturnFormView() throws Exception {
        mockMvc.perform(post("/tasks").sessionAttr("currentUser", adminUser)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "") // Title trống vi phạm @NotBlank
                .param("dueDate", LocalDate.now().plusDays(1).toString())
                .param("priority", "MEDIUM")
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/form"))
            .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("GET /tasks/{id} - Trả về view tasks/detail và danh sách bước chuyển hợp lệ")
    void showTaskDetail_ShouldReturnDetailView() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/tasks/1").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/detail"))
            .andExpect(model().attributeExists("task", "allowedNextStatuses"));
    }

    @Test
    @DisplayName("GET /tasks/{id}/edit - Trả về view tasks/form cho cập nhật")
    void showEditForm_ShouldReturnEditFormView() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/tasks/1/edit").sessionAttr("currentUser", adminUser))
            .andExpect(status().isOk())
            .andExpect(view().name("tasks/form"))
            .andExpect(model().attribute("isEdit", true))
            .andExpect(model().attribute("taskId", 1L));
    }

    @Test
    @DisplayName("POST /tasks/{id} hợp lệ - Áp dụng mẫu PRG chuyển hướng về /tasks/{id}")
    void updateTask_Valid_ShouldRedirect() throws Exception {
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks/1").sessionAttr("currentUser", adminUser)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Tiêu đề đã sửa")
                .param("dueDate", LocalDate.now().plusDays(2).toString())
                .param("priority", "MEDIUM")
                .param("categoryId", "1")
                .param("version", "0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/1"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /tasks/{id}/status - Chuyển trạng thái và redirect về chi tiết task")
    void changeStatus_ShouldRedirect() throws Exception {
        when(taskService.changeStatus(eq(1L), any(TaskStatusUpdateRequest.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks/1/status").sessionAttr("currentUser", adminUser)
                .param("status", "IN_PROGRESS"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/1"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(taskService).changeStatus(eq(1L), any(TaskStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("POST /tasks/{id}/delete - Xóa công việc và redirect về /tasks")
    void deleteTask_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/tasks/1/delete").sessionAttr("currentUser", adminUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(taskService).deleteTask(1L);
    }

    @Test
    @DisplayName("GET /tasks/new khi là MEMBER - Chặn và redirect về /tasks kèm errorMessage")
    void showCreateForm_WhenMember_ShouldRedirectWithErrorMessage() throws Exception {
        UserSummaryResponse memberUser = new UserSummaryResponse(2L, "member", "Member User", "member@corp.com", "MEMBER", "Thành viên");

        mockMvc.perform(get("/tasks/new").sessionAttr("currentUser", memberUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /tasks/1/delete khi là MEMBER - Chặn và redirect về /tasks kèm errorMessage")
    void deleteTask_WhenMember_ShouldRedirectWithErrorMessage() throws Exception {
        UserSummaryResponse memberUser = new UserSummaryResponse(2L, "member", "Member User", "member@corp.com", "MEMBER", "Thành viên");

        mockMvc.perform(post("/tasks/1/delete").sessionAttr("currentUser", memberUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}
