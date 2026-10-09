package com.example.taskmanager.controller.view;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.common.validation.OnCreate;
import com.example.taskmanager.common.validation.OnUpdate;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.request.TaskStatusUpdateRequest;
import com.example.taskmanager.dto.response.KanbanColumnResponse;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller điều hướng giao diện Web Thymeleaf cho thực thể Task.
 * Tái sử dụng 100% tầng TaskService chung với TaskApiController.
 *
 * Nguyên tắc thiết kế:
 * - Áp dụng mẫu Post/Redirect/Get (PRG) cho các thao tác gửi form để ngăn chặn việc người dùng
 *   vô tình bấm F5 submit form lặp lại nhiều lần (Duplicate Form Submission).
 * - Sử dụng RedirectAttributes để truyền thông báo thành công / thất bại (Flash Attributes).
 */
@Slf4j
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskViewController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final UserService userService;

    /**
     * Hiển thị bảng danh sách công việc có bộ lọc đa tiêu chí, phân trang và sắp xếp.
     */
    @GetMapping
    public String listTasks(@ModelAttribute("filter") TaskFilterRequest filter, Model model) {
        PageResponse<TaskResponse> taskPage = taskService.getTasks(filter);

        model.addAttribute("activeNav", "tasks");
        model.addAttribute("taskPage", taskPage);
        populateCommonModelAttributes(model);

        return "tasks/list";
    }

    /**
     * Hiển thị bảng Kanban kéo-thả trực quan 4 cột theo trạng thái,
     * tích hợp dòng thời gian Task Calendar ở phía trên.
     */
    @GetMapping("/kanban")
    public String kanbanBoard(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate,
        HttpSession session,
        Model model
    ) {
        List<KanbanColumnResponse> columns = taskService.getKanbanBoard(categoryId, assigneeId);

        model.addAttribute("activeNav", "kanban");
        model.addAttribute("columns", columns);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAssigneeId", assigneeId);

        // Với dải timeline lịch: Admin thấy tất cả (hoặc theo filter), Member chỉ thấy của mình
        Long timelineAssigneeId = isMemberRole(session) ? getCurrentUserId(session) : assigneeId;
        populateTimelineModel(model, anchorDate, categoryId, timelineAssigneeId);
        populateCommonModelAttributes(model);
        populateKanbanSidePanelModel(model, columns);

        return "tasks/kanban";
    }

    /**
     * Màn hình My Task chuyên biệt theo thiết kế Linear/Asana:
     * - Phía trên: Task Calendar (dòng thời gian công việc của tài khoản hiện tại)
     * - Phía dưới: All Task (Bảng Kanban 4 cột công việc được giao)
     */
    @GetMapping("/my-tasks")
    public String myTasks(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate,
        HttpSession session,
        Model model
    ) {
        Long currentUserId = getCurrentUserId(session);

        String view = kanbanBoard(categoryId, currentUserId, anchorDate, session, model);
        model.addAttribute("activeNav", "my-tasks");
        model.addAttribute("isMyTasksView", true);
        return view;
    }

    private void populateTimelineModel(Model model, LocalDate anchorDate, Long categoryId, Long assigneeId) {
        LocalDate anchor = (anchorDate != null) ? anchorDate : LocalDate.now();
        // Cửa sổ 8 ngày như mockup: anchor - 3 ngày đến anchor + 4 ngày
        LocalDate start = anchor.minusDays(3);
        LocalDate end = anchor.plusDays(4);

        List<LocalDate> timelineDays = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            timelineDays.add(d);
        }

        TaskFilterRequest timelineFilter = TaskFilterRequest.builder()
            .dueFrom(start)
            .dueTo(end)
            .categoryId(categoryId)
            .assigneeId(assigneeId)
            .page(0)
            .size(100)
            .sortBy("dueDate")
            .sortDirection("asc")
            .build();

        PageResponse<TaskResponse> timelineTasks = null;
        try {
            timelineTasks = taskService.getTasks(timelineFilter);
        } catch (Exception ex) {
            log.debug("Error fetching timeline tasks: {}", ex.getMessage());
        }

        Map<LocalDate, List<TaskResponse>> timelineTasksByDate = new HashMap<>();
        if (timelineTasks != null && timelineTasks.content() != null) {
            for (TaskResponse t : timelineTasks.content()) {
                if (t.dueDate() != null) {
                    timelineTasksByDate.computeIfAbsent(t.dueDate(), k -> new ArrayList<>()).add(t);
                }
            }
        }

        model.addAttribute("timelineAnchor", anchor);
        model.addAttribute("timelineDays", timelineDays);
        model.addAttribute("timelineTasksByDate", timelineTasksByDate);
        model.addAttribute("timelinePrevAnchor", anchor.minusDays(7));
        model.addAttribute("timelineNextAnchor", anchor.plusDays(7));
        model.addAttribute("todayDate", LocalDate.now());
    }

    /**
     * Hiển thị giao diện Lịch công việc (Task Calendar & Timeline) theo tháng.
     * - Admin: Nhìn thấy toàn bộ công việc trong hệ thống (có thể chọn lọc theo nhân viên hoặc xem tất cả).
     * - Thành viên (Member): Chỉ nhìn thấy công việc được giao cho chính mình.
     */
    @GetMapping("/calendar")
    public String taskCalendar(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long assigneeId,
        HttpSession session,
        Model model
    ) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int targetYear = (year != null && year >= 2000 && year <= 2100) ? year : now.getYear();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
        java.time.YearMonth currentYearMonth = java.time.YearMonth.of(targetYear, targetMonth);

        java.time.LocalDate startDate = currentYearMonth.atDay(1);
        java.time.LocalDate endDate = currentYearMonth.atEndOfMonth();

        boolean isMember = isMemberRole(session);
        Long currentUserId = getCurrentUserId(session);
        Long effectiveAssigneeId = isMember ? currentUserId : assigneeId;

        TaskFilterRequest filter = TaskFilterRequest.builder()
            .dueFrom(startDate)
            .dueTo(endDate)
            .categoryId(categoryId)
            .assigneeId(effectiveAssigneeId)
            .page(0)
            .size(100)
            .sortBy("dueDate")
            .sortDirection("asc")
            .build();

        PageResponse<TaskResponse> taskPage = taskService.getTasks(filter);
        List<TaskResponse> monthTasks = (taskPage != null && taskPage.content() != null)
            ? taskPage.content()
            : Collections.emptyList();

        // Gom nhóm tasks theo ngày hết hạn (dueDate)
        java.util.Map<java.time.LocalDate, java.util.List<TaskResponse>> tasksByDate = new java.util.HashMap<>();
        for (TaskResponse t : monthTasks) {
            if (t.dueDate() != null) {
                tasksByDate.computeIfAbsent(t.dueDate(), k -> new java.util.ArrayList<>()).add(t);
            }
        }

        // Tạo danh sách các ngày trong tháng
        java.util.List<java.time.LocalDate> daysInMonth = new java.util.ArrayList<>();
        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            daysInMonth.add(currentYearMonth.atDay(day));
        }

        model.addAttribute("activeNav", "calendar");
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("targetYear", targetYear);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("prevYearMonth", currentYearMonth.minusMonths(1));
        model.addAttribute("nextYearMonth", currentYearMonth.plusMonths(1));
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("tasksByDate", tasksByDate);
        model.addAttribute("monthTasks", monthTasks);
        model.addAttribute("totalMonthTasks", monthTasks.size());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAssigneeId", effectiveAssigneeId);
        model.addAttribute("isMember", isMember);
        populateCommonModelAttributes(model);

        return "tasks/calendar";
    }

    /**
     * Hiển thị form tạo mới công việc (Chỉ dành cho Admin).
     */
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới được phép tạo công việc!");
            return "redirect:/tasks";
        }

        TaskRequest form = new TaskRequest();
        form.setPriority(Priority.MEDIUM);

        model.addAttribute("activeNav", "tasks");
        model.addAttribute("taskRequest", form);
        model.addAttribute("isEdit", false);
        populateCommonModelAttributes(model);

        return "tasks/form";
    }

    /**
     * Xử lý gửi form tạo mới công việc (Chỉ dành cho Admin).
     * Áp dụng nhóm validation OnCreate để bắt buộc dueDate >= hôm nay.
     */
    @PostMapping
    public String createTask(
        @Validated(OnCreate.class) @ModelAttribute("taskRequest") TaskRequest taskRequest,
        BindingResult bindingResult,
        Model model,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (isMemberRole(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới được phép tạo công việc!");
            return "redirect:/tasks";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "tasks");
            model.addAttribute("isEdit", false);
            populateCommonModelAttributes(model);
            return "tasks/form";
        }

        taskService.createTask(taskRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo mới công việc thành công!");
        return "redirect:/tasks";
    }

    /**
     * Hiển thị chi tiết một công việc kèm các nút chuyển trạng thái hợp lệ.
     */
    @GetMapping("/{id}")
    public String showTaskDetail(@PathVariable Long id, Model model) {
        TaskResponse task = taskService.getTaskById(id);

        model.addAttribute("activeNav", "tasks");
        model.addAttribute("task", task);
        model.addAttribute("allowedNextStatuses", task.status().allowedNextStatuses());

        return "tasks/detail";
    }

    /**
     * Hiển thị form chỉnh sửa nội dung công việc (Chỉ dành cho Admin).
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền chỉnh sửa công việc!");
            return "redirect:/tasks/" + id;
        }

        TaskResponse task = taskService.getTaskById(id);

        TaskRequest form = TaskRequest.builder()
            .title(task.title())
            .description(task.description())
            .dueDate(task.dueDate())
            .priority(task.priority())
            .categoryId(task.category() != null ? task.category().id() : null)
            .assigneeId(task.assignee() != null ? task.assignee().id() : null)
            .version(task.version())
            .build();

        model.addAttribute("activeNav", "tasks");
        model.addAttribute("taskId", id);
        model.addAttribute("taskRequest", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("taskStatus", task.status());
        populateCommonModelAttributes(model);

        return "tasks/form";
    }

    /**
     * Xử lý gửi form cập nhật nội dung công việc (Chỉ dành cho Admin).
     * Áp dụng nhóm validation OnUpdate để không chặn task đã quá hạn.
     */
    @PostMapping("/{id}")
    public String updateTask(
        @PathVariable Long id,
        @Validated(OnUpdate.class) @ModelAttribute("taskRequest") TaskRequest taskRequest,
        BindingResult bindingResult,
        Model model,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (isMemberRole(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền chỉnh sửa công việc!");
            return "redirect:/tasks/" + id;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "tasks");
            model.addAttribute("taskId", id);
            model.addAttribute("isEdit", true);
            populateCommonModelAttributes(model);
            return "tasks/form";
        }

        taskService.updateTask(id, taskRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nội dung công việc thành công!");
        return "redirect:/tasks/" + id;
    }

    /**
     * Xử lý chuyển đổi trạng thái công việc thông qua nút bấm trên trang chi tiết (Cả Admin và Member).
     */
    @PostMapping("/{id}/status")
    public String changeStatus(
        @PathVariable Long id,
        @RequestParam TaskStatus status,
        RedirectAttributes redirectAttributes
    ) {
        taskService.changeStatus(id, new TaskStatusUpdateRequest(status, null));
        redirectAttributes.addFlashAttribute("successMessage", "Chuyển trạng thái sang '" + status.getLabel() + "' thành công!");
        return "redirect:/tasks/" + id;
    }

    /**
     * Xử lý xóa công việc từ giao diện web (Chỉ dành cho Admin).
     */
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (isMemberRole(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này. Chỉ Quản trị viên mới có quyền xóa công việc!");
            return "redirect:/tasks";
        }

        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa công việc thành công!");
        return "redirect:/tasks";
    }

    private boolean isMemberRole(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object userObj = session.getAttribute("currentUser");
        if (userObj instanceof UserSummaryResponse user) {
            return "MEMBER".equalsIgnoreCase(user.role());
        }
        return false;
    }

    private Long getCurrentUserId(HttpSession session) {
        if (session != null) {
            Object uid = session.getAttribute("currentUserId");
            if (uid instanceof Long) {
                return (Long) uid;
            }
            Object userObj = session.getAttribute("currentUser");
            if (userObj instanceof UserSummaryResponse user) {
                return user.id();
            }
        }
        if (userService != null) {
            try {
                List<UserSummaryResponse> users = userService.getAllUsers();
                if (users != null && !users.isEmpty()) {
                    return users.get(0).id();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void populateCommonModelAttributes(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", Priority.values());
    }

    private void populateKanbanSidePanelModel(Model model, List<KanbanColumnResponse> columns) {
        if (columns == null) {
            model.addAttribute("kanbanCategoryProgress", java.util.Collections.emptyList());
            model.addAttribute("kanbanRecentActivities", java.util.Collections.emptyList());
            return;
        }

        // Lấy tất cả task từ các cột
        List<TaskResponse> allTasks = columns.stream()
            .filter(c -> c.tasks() != null)
            .flatMap(c -> c.tasks().stream())
            .toList();

        // Lấy các task đã hoàn thành (DONE)
        List<TaskResponse> doneTasks = columns.stream()
            .filter(c -> c.status() == TaskStatus.DONE && c.tasks() != null)
            .flatMap(c -> c.tasks().stream())
            .toList();

        // 1. Task Progress theo từng danh mục (Mẫu Ảnh 1: Copywriting 3/8, Illustrations 6/10, UI Design 2/7)
        List<com.example.taskmanager.dto.response.CategoryResponse> categories =
            (categoryService != null) ? categoryService.getAllCategories() : java.util.Collections.emptyList();

        List<Map<String, Object>> progressList = new ArrayList<>();
        if (categories != null) {
            for (var cat : categories) {
                long totalInCat = allTasks.stream()
                    .filter(t -> t.category() != null && t.category().id().equals(cat.id()))
                    .count();
                long doneInCat = doneTasks.stream()
                    .filter(t -> t.category() != null && t.category().id().equals(cat.id()))
                    .count();
                int pct = totalInCat > 0 ? (int) Math.round(((double) doneInCat / totalInCat) * 100) : 0;

                Map<String, Object> item = new HashMap<>();
                item.put("id", cat.id());
                item.put("name", cat.name());
                item.put("colorHex", cat.colorHex() != null ? cat.colorHex() : "#ec4899");
                item.put("doneCount", doneInCat);
                item.put("totalCount", totalInCat);
                item.put("percentage", pct);
                progressList.add(item);
            }
        }
        model.addAttribute("kanbanCategoryProgress", progressList);

        // 2. Recent Activities (Mẫu Ảnh 1: Andrea uploaded 3 documents, Karen leave comments...)
        List<TaskResponse> recentActivities = allTasks.stream()
            .sorted((a, b) -> {
                if (a.updatedAt() != null && b.updatedAt() != null) {
                    return b.updatedAt().compareTo(a.updatedAt());
                }
                return Long.compare(b.id(), a.id());
            })
            .limit(5)
            .toList();
        model.addAttribute("kanbanRecentActivities", recentActivities);
    }
}
