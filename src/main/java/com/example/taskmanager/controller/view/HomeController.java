package com.example.taskmanager.controller.view;

import com.example.taskmanager.common.PageResponse;
import com.example.taskmanager.dto.request.TaskFilterRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.taskmanager.dto.response.CategoryResponse;
import com.example.taskmanager.service.CategoryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller điều hướng trang chủ và hiển thị Bảng điều khiển (Dashboard).
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    /**
     * Chuyển hướng từ đường dẫn gốc '/':
     * - Admin -> Dashboard
     * - Member -> My Task
     */
    @GetMapping("/")
    public String index(HttpSession session) {
        if (isMember(session)) {
            return "redirect:/tasks/my-tasks";
        }
        return "redirect:/dashboard";
    }

    /**
     * Hiển thị bảng điều khiển tổng quan với các chỉ số thống kê công việc và tiến độ (Chỉ dành cho Admin).
     * Thành viên không có Dashboard, tự động chuyển về /tasks/my-tasks.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (isMember(session)) {
            return "redirect:/tasks/my-tasks";
        }
        long todoCount = taskService.countByStatus(TaskStatus.TODO);
        long inProgressCount = taskService.countByStatus(TaskStatus.IN_PROGRESS);
        long reviewCount = taskService.countByStatus(TaskStatus.REVIEW);
        long doneCount = taskService.countByStatus(TaskStatus.DONE);
        long totalTasks = todoCount + inProgressCount + reviewCount + doneCount;
        long overdueCount = taskService.countOverdueTasks();
        long upcomingCount = taskService.countUpcomingTasks();

        int completionRate = totalTasks > 0 ? (int) Math.round(((double) doneCount / totalTasks) * 100) : 0;
        int todoPercent = totalTasks > 0 ? (int) Math.round(((double) todoCount / totalTasks) * 100) : 0;
        int inProgressPercent = totalTasks > 0 ? (int) Math.round(((double) inProgressCount / totalTasks) * 100) : 0;
        int reviewPercent = totalTasks > 0 ? (int) Math.round(((double) reviewCount / totalTasks) * 100) : 0;
        int donePercent = totalTasks > 0 ? (int) Math.round(((double) doneCount / totalTasks) * 100) : 0;
        long remainingTasks = totalTasks - doneCount;

        // Truyền các biến thống kê cho giao diện Dashboard
        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("totalCount", totalTasks);
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("todoPercent", todoPercent);
        model.addAttribute("inProgressPercent", inProgressPercent);
        model.addAttribute("reviewPercent", reviewPercent);
        model.addAttribute("donePercent", donePercent);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("remainingTasks", remainingTasks);

        // Lấy danh sách 5 công việc đã hoàn thành gần đây để hiển thị lên Dashboard
        TaskFilterRequest completedFilter = TaskFilterRequest.builder()
            .status(TaskStatus.DONE)
            .page(0)
            .size(5)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
        PageResponse<TaskResponse> completedPage = taskService.getTasks(completedFilter);
        model.addAttribute("recentCompletedTasks", completedPage != null ? completedPage.content() : java.util.Collections.emptyList());

        // Lấy danh sách 5 công việc đang thực hiện
        TaskFilterRequest inProgressFilter = TaskFilterRequest.builder()
            .status(TaskStatus.IN_PROGRESS)
            .page(0)
            .size(5)
            .sortBy("dueDate")
            .sortDirection("ASC")
            .build();
        PageResponse<TaskResponse> inProgressPage = taskService.getTasks(inProgressFilter);
        model.addAttribute("recentInProgressTasks", inProgressPage != null ? inProgressPage.content() : java.util.Collections.emptyList());

        // Lấy danh sách 6 công việc mới nhất cho bảng phân tích (Top Vectors Analysis theo Mẫu Ảnh 2)
        TaskFilterRequest topFilter = TaskFilterRequest.builder()
            .page(0)
            .size(6)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
        try {
            PageResponse<TaskResponse> topTasksPage = taskService.getTasks(topFilter);
            model.addAttribute("topTasks", topTasksPage != null ? topTasksPage.content() : java.util.Collections.emptyList());
        } catch (Exception ignored) {
            model.addAttribute("topTasks", java.util.Collections.emptyList());
        }

        // Thống kê phân bổ theo danh mục dự án (Category Distribution theo Mẫu Ảnh 2)
        List<CategoryResponse> categories = categoryService != null ? categoryService.getAllCategories() : java.util.Collections.emptyList();
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        if (categories != null) {
            for (CategoryResponse cat : categories) {
                int pct = totalTasks > 0 && cat.taskCount() != null ? (int) Math.min(100, Math.round(((double) cat.taskCount() / totalTasks) * 100)) : 0;
                Map<String, Object> map = new HashMap<>();
                map.put("name", cat.name());
                map.put("colorHex", cat.colorHex() != null ? cat.colorHex() : "#4F46E5");
                map.put("taskCount", cat.taskCount() != null ? cat.taskCount() : 0L);
                map.put("percentage", pct);
                categoryStats.add(map);
            }
        }
        model.addAttribute("categoryStats", categoryStats);

        // Phễu tiến độ tác vụ (Resolution Funnel theo Mẫu Ảnh 2)
        int funnelStage1Pct = 100;
        int funnelStage2Pct = totalTasks > 0 ? (int) Math.round(((double) (totalTasks - todoCount) / totalTasks) * 100) : 75;
        int funnelStage3Pct = totalTasks > 0 ? (int) Math.round(((double) (doneCount + reviewCount) / totalTasks) * 100) : 50;
        int funnelStage4Pct = completionRate;
        model.addAttribute("funnelStage1Pct", funnelStage1Pct);
        model.addAttribute("funnelStage2Pct", Math.max(15, funnelStage2Pct));
        model.addAttribute("funnelStage3Pct", Math.max(15, funnelStage3Pct));
        model.addAttribute("funnelStage4Pct", Math.max(15, funnelStage4Pct));

        return "dashboard";
    }

    private boolean isMember(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object userObj = session.getAttribute("currentUser");
        if (userObj instanceof UserSummaryResponse user) {
            return "MEMBER".equalsIgnoreCase(user.role());
        }
        return false;
    }
}
