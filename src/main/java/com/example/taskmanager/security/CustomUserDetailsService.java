package com.example.taskmanager.security;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dịch vụ nạp thông tin người dùng từ cơ sở dữ liệu cho Spring Security.
 * Hỗ trợ đăng nhập linh hoạt bằng cả Tên người dùng (username) hoặc Email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Đang xác thực và nạp thông tin tài khoản: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy tài khoản với tên đăng nhập hoặc email: " + usernameOrEmail
            ));

        return new CustomUserDetails(user);
    }
}
