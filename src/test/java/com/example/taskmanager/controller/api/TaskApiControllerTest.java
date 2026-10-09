package com.example.taskmanager.controller.api;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.exception.ApiExceptionHandler;
import com.example.taskmanager.exception.InvalidStatusTransitionException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử cắt lớp Web (WebMvc Slice Test) cho TaskApiController sử dụng MockMvc.
 * Chỉ nạp duy nhất tầng Web (Controller + Exception Handler), giả lập hoàn toàn TaskService.
 * Giúp kiểm thử nhanh chóng các mã trạng thái HTTP, Header, Validation và định dạng JSON.
 */
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(TaskApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class) // Nạp tầng xử lý lỗi tập trung để kiểm thử toàn diện các case ngoại lệ
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new TaskResponse(
            100L,
            "Tích hợp cổng thanh toán",
            "Mô tả chi tiết",
            LocalDate.now().plusDays(5),
            Priority.HIGH,
            "Cao",
            "bg-danger-subtle text-danger",
            TaskStatus.TODO,
            "Cần làm",
            "bg-secondary-subtle text-secondary",
            null,
            new CategoryResponse(1L, "Payment", "Mô tả", "#4F46E5", 1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            0L,
            false
        );
    }

    @Test
    @DisplayName("GET /api/v1/tasks trả về HTTP 200 OK kèm dữ liệu phân trang ApiResponse")
    void getTasks_returns200() throws Exception {
        PageResponse<TaskResponse> pageResponse = new PageResponse<>(
            List.of(sampleResponse), 0, 10, 1L, 1, true, true
        );
        when(taskService.getTasks(any(TaskFilterRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content", hasSize(1)))
            .andExpect(jsonPath("$.data.content[0].title").value("Tích hợp cổng thanh toán"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} trả về HTTP 200 OK khi tìm thấy")
    void getTaskById_found_returns200() throws Exception {
        when(taskService.getTaskById(100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/tasks/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} trả về HTTP 404 NOT FOUND khi không tồn tại")
    void getTaskById_notFound_returns404() throws Exception {
        when(taskService.getTaskById(99999L)).thenThrow(new ResourceNotFoundException("Công việc", 99999L));

        mockMvc.perform(get("/api/v1/tasks/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Không tìm thấy Công việc với định danh: 99999")));
    }

    @Test
    @DisplayName("POST /api/v1/tasks thành công trả về HTTP 201 CREATED kèm header Location")
    void createTask_valid_returns201WithLocation() throws Exception {
        TaskRequest validRequest = TaskRequest.builder()
            .title("Task mới")
            .description("Mô tả")
            .priority(Priority.HIGH)
            .categoryId(1L)
            .dueDate(LocalDate.now().plusDays(2))
            .build();

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/api/v1/tasks/100")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/tasks dữ liệu rỗng trả về HTTP 400 BAD REQUEST kèm danh sách errors chi tiết")
    void createTask_invalid_returns400WithErrors() throws Exception {
        TaskRequest invalidRequest = new TaskRequest(); // Toàn bộ trường đều null/rỗng

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Dữ liệu không hợp lệ"))
            .andExpect(jsonPath("$.errors.title").exists())
            .andExpect(jsonPath("$.errors.priority").exists())
            .andExpect(jsonPath("$.errors.categoryId").exists())
            .andExpect(jsonPath("$.errors.dueDate").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status chuyển trạng thái sai quy tắc trả về HTTP 422 UNPROCESSABLE ENTITY")
    void changeStatus_invalidTransition_returns422() throws Exception {
        TaskStatusUpdateRequest statusRequest = new TaskStatusUpdateRequest(TaskStatus.DONE, 0L);

        when(taskService.changeStatus(eq(100L), any(TaskStatusUpdateRequest.class)))
            .thenThrow(new InvalidStatusTransitionException(TaskStatus.TODO, TaskStatus.DONE));

        mockMvc.perform(patch("/api/v1/tasks/100/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Không thể chuyển từ trạng thái 'Cần làm' sang 'Hoàn thành'")));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} trả về HTTP 204 NO CONTENT khi xóa thành công")
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(100L);

        mockMvc.perform(delete("/api/v1/tasks/100"))
            .andExpect(status().isNoContent());
    }
}
