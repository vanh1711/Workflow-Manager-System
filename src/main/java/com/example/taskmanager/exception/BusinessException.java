package com.example.taskmanager.exception;

import lombok.Getter;

/**
 * Lớp ngoại lệ cơ sở cho toàn bộ các lỗi nghiệp vụ trong hệ thống (Base Business Exception).
 * Kế thừa {@link RuntimeException} (Unchecked Exception) để Spring Boot tự động kích hoạt
 * cơ chế Rollback Transaction khi ngoại lệ này được ném ra.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
