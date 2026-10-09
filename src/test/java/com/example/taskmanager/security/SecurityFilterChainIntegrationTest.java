package com.example.taskmanager.security;

import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử tích hợp chuỗi bộ lọc bảo mật Spring Security 6 (SecurityFilterChain Integration Test).
 * Xác thực các quy tắc:
 * - Bảo vệ URL & chuyển hướng đăng nhập
 * - Phân quyền RBAC (ROLE_ADMIN vs ROLE_MEMBER)
 * - Tự động xử lý đăng xuất an toàn
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /login - Trang đăng nhập cho phép truy cập công khai 200 OK")
    void loginPage_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dashboard khi chưa đăng nhập - Bị chuyển hướng 302 về /login")
    void dashboard_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("GET /users khi chưa đăng nhập - Bị chuyển hướng 302 về /login")
    void usersPage_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("GET /dashboard khi là MEMBER - Bị AccessDeniedHandler chuyển hướng về /tasks/my-tasks?accessDenied=true")
    void dashboard_WhenMember_ShouldBeDenied() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/my-tasks?accessDenied=true"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /dashboard khi là ADMIN - Được phép truy cập thành công 200 OK")
    void dashboard_WhenAdmin_ShouldBeAllowed() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /logout - Thực hiện đăng xuất và chuyển hướng về /login?logout=true")
    void logout_ShouldRedirectToLoginWithParam() throws Exception {
        mockMvc.perform(get("/logout"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout=true"));
    }
}
