package com.example.taskmanager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi nghiệp vụ chuẩn hóa toàn hệ thống.
 * Mỗi mã lỗi gắn liền với một mã trạng thái HTTP (HttpStatus) và thông điệp mặc định tiếng Việt.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 BAD REQUEST
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Dữ liệu yêu cầu không hợp lệ"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ: 'Từ ngày' phải nhỏ hơn hoặc bằng 'Đến ngày'"),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "Trường sắp xếp không hợp lệ"),

    // 401 UNAUTHORIZED / 403 FORBIDDEN
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập vào chức năng này"),

    // 404 NOT FOUND
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu"),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy công việc"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),

    // 409 CONFLICT
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại trong hệ thống"),
    CATEGORY_NAME_DUPLICATE(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại"),
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "Email đã tồn tại"),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "Danh mục đang chứa các công việc liên kết, không thể xóa"),
    TASK_COMPLETED_CANNOT_EDIT(HttpStatus.CONFLICT, "Công việc đã ở trạng thái Hoàn thành, không được phép chỉnh sửa nội dung"),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "Dữ liệu vừa được người dùng khác cập nhật, vui lòng tải lại trang để thấy dữ liệu mới nhất"),

    // 422 UNPROCESSABLE ENTITY
    INVALID_STATUS_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY, "Chuyển đổi trạng thái công việc không hợp lệ"),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi nội bộ máy chủ, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
