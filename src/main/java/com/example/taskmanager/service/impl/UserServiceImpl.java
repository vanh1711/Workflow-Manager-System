package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.request.UserCreateRequest;
import com.example.taskmanager.dto.response.UserSummaryResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.DuplicateResourceException;
import com.example.taskmanager.exception.ErrorCode;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ Người dùng.
 * Áp dụng {@code @Transactional(readOnly = true)} ở mức class để Hibernate tối ưu hóa dirty-checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Mặc định chỉ đọc: tối ưu hiệu năng đọc dữ liệu từ DB
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(TaskMapper::toUserSummary)
            .toList();
    }

    @Override
    public UserSummaryResponse getUserById(Long id) {
        return userRepository.findById(id)
            .map(TaskMapper::toUserSummary)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserSummaryResponse authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập và mật khẩu không được để trống");
        }

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail.trim(), usernameOrEmail.trim())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Đăng nhập thất bại cho tài khoản: {}", usernameOrEmail);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        log.info("Xác thực thành công cho người dùng: {} ({})", user.getUsername(), user.getRole());
        return TaskMapper.toUserSummary(user);
    }

    @Override
    @Transactional
    public UserSummaryResponse createUser(UserCreateRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException(ErrorCode.USERNAME_DUPLICATE, "Tên đăng nhập '" + username + "' đã được sử dụng");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(ErrorCode.EMAIL_DUPLICATE, "Email '" + email + "' đã được sử dụng");
        }

        User newUser = User.builder()
            .username(username)
            .email(email)
            .fullName(request.getFullName().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .build();

        User savedUser = userRepository.save(newUser);
        log.info("Quản trị viên đã cấp tài khoản mới: {} (ID: {}, Role: {})", savedUser.getUsername(), savedUser.getId(), savedUser.getRole());
        return TaskMapper.toUserSummary(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "Vui lòng đăng nhập để thực hiện đổi mật khẩu");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu hiện tại không chính xác");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Người dùng '{}' (ID: {}) đã đổi mật khẩu thành công", user.getUsername(), user.getId());
    }
}
