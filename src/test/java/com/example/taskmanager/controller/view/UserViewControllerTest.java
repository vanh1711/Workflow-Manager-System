package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.UserCreateRequest;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    private UserSummaryResponse adminUser;
    private UserSummaryResponse memberUser;
    private TestingAuthenticationToken adminAuth;
    private TestingAuthenticationToken memberAuth;

    @BeforeEach
    void setUp() {
        adminUser = new UserSummaryResponse(1L, "admin", "Admin System", "admin@corp.com", Role.ADMIN.name(), "Quản trị");
        memberUser = new UserSummaryResponse(2L, "dev_user", "Dev User", "dev@corp.com", Role.MEMBER.name(), "Thành viên");
        adminAuth = new TestingAuthenticationToken("admin", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        memberAuth = new TestingAuthenticationToken("dev_user", "pass", List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
        when(userService.getAllUsers()).thenReturn(List.of(adminUser, memberUser));
    }

    @Test
    @DisplayName("GET /users - Admin truy cập xem danh sách thành viên nội bộ thành công 200 OK")
    void listUsers_Admin_ShouldReturnListView() throws Exception {
        mockMvc.perform(get("/users").principal(adminAuth))
            .andExpect(status().isOk())
            .andExpect(view().name("users/list"))
            .andExpect(model().attributeExists("usersList", "userCreateRequest"));
    }

    @Test
    @DisplayName("GET /users - Member bị chặn truy cập và chuyển hướng về /tasks/my-tasks")
    void listUsers_Member_ShouldRedirectWithErrorMessage() throws Exception {
        mockMvc.perform(get("/users").principal(memberAuth))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/tasks/my-tasks"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /users - Admin cấp tài khoản mới hợp lệ thành công chuyển hướng về /users")
    void createUser_Valid_ShouldRedirectWithSuccessMessage() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(memberUser);

        mockMvc.perform(post("/users").principal(adminAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "intern_dev")
                .param("fullName", "Thực tập sinh")
                .param("email", "intern@bank.corp")
                .param("password", "intern123")
                .param("role", "MEMBER"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("POST /users - Form không hợp lệ trả về view users/list kèm thông báo lỗi")
    void createUser_Invalid_ShouldReturnListView() throws Exception {
        mockMvc.perform(post("/users").principal(adminAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "") // username rỗng
                .param("fullName", "Test")
                .param("email", "invalid-email")
                .param("password", "123") // mật khẩu quá ngắn
                .param("role", "MEMBER"))
            .andExpect(status().isOk())
            .andExpect(view().name("users/list"))
            .andExpect(model().hasErrors());
    }
}
