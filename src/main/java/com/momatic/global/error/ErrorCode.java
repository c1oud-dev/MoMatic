package com.momatic.global.error;

import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드를 정의하는 열거형입니다.
 */
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UPLOAD_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 형식입니다."),
    UPLOAD_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "플랜 허용 파일 크기를 초과했습니다."),
    UPLOAD_MONTHLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "월 업로드 가능 횟수를 초과했습니다.");

    private final HttpStatus status;

    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }

    public String getMessage() { return message; }
}
