package com.momatic.global.error;

import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드를 정의하는 열거형입니다.
 */
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 구성원을 찾을 수 없습니다."),
    TEAM_INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 초대를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PLAN_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 플랜입니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다."),
    TEAM_PLAN_REQUIRED(HttpStatus.FORBIDDEN, "팀 플랜 사용자만 팀을 생성할 수 있습니다."),
    TEAM_MEMBER_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "팀 구성원은 최대 10명까지 가능합니다."),
    TEAM_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팀에 가입된 사용자입니다."),
    TEAM_INVITE_EXPIRED(HttpStatus.GONE, "만료된 팀 초대입니다."),
    TEAM_INVITE_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "이미 수락된 팀 초대입니다."),
    TEAM_OWNER_SELF_REMOVE_DENIED(HttpStatus.BAD_REQUEST, "OWNER는 자기 자신을 추방할 수 없습니다."),
    INVALID_TEAM_ROLE(HttpStatus.BAD_REQUEST, "지원하지 않는 팀 권한입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "결제 승인에 실패했습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 올바르지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "처리할 수 없는 결제 상태입니다."),
    INVALID_PAYMENT_WEBHOOK(HttpStatus.BAD_REQUEST, "올바르지 않은 결제 Webhook입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UPLOAD_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 형식입니다."),
    UPLOAD_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "플랜 허용 파일 크기를 초과했습니다. 요금제를 업그레이드해 주세요."),
    UPLOAD_MONTHLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "월 업로드 가능 횟수를 초과했습니다. 요금제를 업그레이드해 주세요.");

    private final HttpStatus status;

    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * HTTP 상태를 조회합니다.
     *
     * @return HTTP 상태
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * 사용자용 에러 메시지를 조회합니다.
     *
     * @return 사용자용 에러 메시지
     */
    public String getMessage() {
        return message;
    }
}
