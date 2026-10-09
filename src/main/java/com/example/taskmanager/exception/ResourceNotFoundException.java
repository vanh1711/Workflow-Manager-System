package com.example.taskmanager.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên yêu cầu (tương ứng HTTP 404 NOT FOUND).
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, String.format("Không tìm thấy %s với định danh: %s", resourceName, identifier));
    }

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
