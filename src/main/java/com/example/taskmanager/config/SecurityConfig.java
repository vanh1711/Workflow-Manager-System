package com.example.taskmanager.config;

import com.example.taskmanager.security.CustomAccessDeniedHandler;
import com.example.taskmanager.security.CustomAuthenticationSuccessHandler;
import com.example.taskmanager.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình trung tâm Spring Security 6 cho toàn bộ hệ thống TaskFlow Enterprise.
 * Áp dụng mô hình Session-based FormLogin bảo mật chuẩn OWASP, phân quyền đa cấp RBAC (ADMIN / MEMBER),
 * chống tấn công CSRF và điều hướng vai trò thông minh.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Tài nguyên tĩnh, trang đăng nhập và endpoint public
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/error",
                    "/login",
                    "/api/**"
                ).permitAll()

                // 2. Chức năng Quản trị hệ thống (Chỉ dành riêng cho ADMIN)
                .requestMatchers(
                    "/dashboard",
                    "/users/**",
                    "/tasks/new",
                    "/tasks/*/edit",
                    "/tasks/*/delete"
                ).hasRole("ADMIN")

                // 3. Chức năng làm việc nhóm nội bộ (Dành cho cả ADMIN và MEMBER)
                .requestMatchers(
                    "/",
                    "/tasks",
                    "/tasks/my-tasks",
                    "/tasks/kanban",
                    "/tasks/calendar",
                    "/tasks/{id}",
                    "/tasks/{id}/status",
                    "/categories/**"
                ).hasAnyRole("ADMIN", "MEMBER")

                // 4. Mọi yêu cầu còn lại bắt buộc phải xác thực
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usernameOrEmail")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
