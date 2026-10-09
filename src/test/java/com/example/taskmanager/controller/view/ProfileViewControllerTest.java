package com.example.taskmanager.controller.view;

import com.example.taskmanager.dto.request.ChangePasswordRequest;
import com.example.taskmanager.security.CustomUserDetails;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử tầng giao diện Đổi mật khẩu cá nhân (ProfileViewController).
 */
@WebMvcTest(ProfileViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /profile/change-password - Trả về form đổi mật khẩu")
    void showChangePasswordForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/profile/change-password"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/change-password"))
            .andExpect(model().attributeExists("changePasswordRequest"));
    }

    @Test
    @DisplayName("POST /profile/change-password thành công - Đổi mật khẩu và redirect kèm thông báo")
    void changePassword_Success_ShouldRedirect() throws Exception {
        com.example.taskmanager.entity.User userEntity = com.example.taskmanager.entity.User.builder()
            .username("admin")
            .fullName("Admin User")
            .email("admin@test.com")
            .role(com.example.taskmanager.enums.Role.ADMIN)
            .build();
        userEntity.setId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(userEntity);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockMvc.perform(post("/profile/change-password")
                .principal(auth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "123456")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "newpass123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile/change-password"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(userService).changePassword(1L, "123456", "newpass123");
    }

    @Test
    @DisplayName("POST /profile/change-password lỗi mật khẩu xác nhận không khớp - Trả lại view kèm lỗi")
    void changePassword_Mismatch_ShouldReturnViewWithErrors() throws Exception {
        mockMvc.perform(post("/profile/change-password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("currentPassword", "123456")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "differentpass"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/change-password"))
            .andExpect(model().hasErrors());
    }
}
