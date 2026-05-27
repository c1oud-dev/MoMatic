package com.momatic.global.error;

import org.flywaydb.core.api.ErrorCode;

/**
 * 서비스 공통 커스텀 예외입니다.
 */
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 에러 코드를 기반으로 예외를 생성합니다.
     *
     * @param errorCode 에러 코드
     */
    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}