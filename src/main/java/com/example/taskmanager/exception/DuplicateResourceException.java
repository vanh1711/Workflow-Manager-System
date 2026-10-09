package com.example.taskmanager.exception;

/**
 * Ngoại lệ ném ra khi vi phạm ràng buộc duy nhất (tương ứng HTTP 409 CONFLICT).
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
