package com.momatic.global.error;

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
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드와 원인 예외를 기반으로 예외를 생성합니다.
     *
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public CustomException(ErrorCode errorCode,
                           Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 예외에 연결된 에러 코드를 반환합니다.
     *
     * @return 에러 코드
     */
    public ErrorCode getErrorCode() { return errorCode; }
}