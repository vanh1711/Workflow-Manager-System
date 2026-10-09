package com.example.taskmanager.controller.view;

import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /login - Chưa đăng nhập trả về view auth/login 200 OK")
    void loginPage_WhenNotLoggedIn_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"))
            .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    @DisplayName("GET /login?error=true - Hiển thị thông báo lỗi đăng nhập thất bại")
    void loginPage_WithErrorParam_ShouldDisplayErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("GET /login?logout=true - Hiển thị thông báo đăng xuất an toàn")
    void loginPage_WithLogoutParam_ShouldDisplaySuccessMessage() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"))
            .andExpect(model().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /login - Đã đăng nhập vai trò ADMIN chuyển hướng về /dashboard")
    void loginPage_WhenAdminLoggedIn_ShouldRedirectToDashboard() throws Exception {
        TestingAuthenticationToken adminAuth = new TestingAuthenticationToken(
            "admin", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockMvc.perform(get("/login").principal(adminAuth))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("GET /login - Đã đăng nhập vai trò MEMBER chuyển hướng về /tasks/my-tasks")
    void loginPage_WhenMemberLoggedIn_ShouldRedirectToMyTasks() throws Exception {
        TestingAuthenticationToken memberAuth = new TestingAuthenticationToken(
            "developer", "pass", List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        mockMvc.perform(get("/login").principal(memberAuth))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/my-tasks"));
    }
}
