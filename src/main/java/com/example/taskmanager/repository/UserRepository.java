package com.example.taskmanager.repository;

import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Tầng truy xuất dữ liệu cho thực thể User.
 * Kế thừa {@link JpaRepository} để cung cấp sẵn các thao tác CRUD cơ bản.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm kiếm người dùng theo tên đăng nhập (username).
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm kiếm người dùng theo username HOẶC email (tiện lợi khi đăng nhập).
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Kiểm tra sự tồn tại của username (dùng khi validate tạo tài khoản mới).
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra sự tồn tại của email (dùng khi validate tạo tài khoản mới).
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại cho người dùng khác (dùng khi cập nhật).
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * Kiểm tra email đã tồn tại cho người dùng khác (dùng khi cập nhật).
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}
