package com.example.taskmanager.controller.view;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /dashboard - Render dashboard thành công 200 OK")
    void dashboard_ShouldRenderSuccessfully() throws Exception {
        when(taskService.countByStatus(any())).thenReturn(5L);
        when(taskService.countOverdueTasks()).thenReturn(1L);
        when(taskService.countUpcomingTasks()).thenReturn(2L);

        CategoryResponse cat = new CategoryResponse(1L, "Banking", "Core API", "#4F46E5", 5L);
        UserSummaryResponse user = new UserSummaryResponse(1L, "admin", "Admin User", "admin@corp.com", "ADMIN", "Quản trị");

        TaskResponse sample = new TaskResponse(
            1L, "Task 1", "Desc", LocalDate.now(), Priority.HIGH, "Cao", "badge-high",
            TaskStatus.DONE, "Hoàn thành", "badge-done", user, cat,
            LocalDateTime.now(), LocalDateTime.now(), 0L, false
        );

        when(taskService.getTasks(any())).thenReturn(new PageResponse<>(List.of(sample), 0, 5, 1L, 1, true, true));
        when(categoryService.getAllCategories()).thenReturn(List.of(cat));
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/dashboard").sessionAttr("currentUser", user))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard"))
            .andExpect(model().attributeExists("totalTasks", "doneCount", "completionRate"));
    }

    @Test
    @DisplayName("GET /dashboard khi là MEMBER - Chuyển hướng sang /tasks/my-tasks")
    void dashboard_WhenMember_ShouldRedirectToMyTasks() throws Exception {
        UserSummaryResponse memberUser = new UserSummaryResponse(2L, "member", "Member User", "member@corp.com", "MEMBER", "Thành viên");

        mockMvc.perform(get("/dashboard").sessionAttr("currentUser", memberUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/my-tasks"));
    }

    @Test
    @DisplayName("GET / khi là MEMBER - Chuyển hướng sang /tasks/my-tasks")
    void index_WhenMember_ShouldRedirectToMyTasks() throws Exception {
        UserSummaryResponse memberUser = new UserSummaryResponse(2L, "member", "Member User", "member@corp.com", "MEMBER", "Thành viên");

        mockMvc.perform(get("/").sessionAttr("currentUser", memberUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/my-tasks"));
    }
}
