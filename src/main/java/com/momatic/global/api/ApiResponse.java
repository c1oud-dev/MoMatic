package com.momatic.global.api;

/**
 * API 공통 응답 래퍼를 표현하는 레코드입니다.
 *
 * @param success 요청 성공 여부
 * @param data 응답 데이터
 * @param errorCode 에러 코드
 * @param message 응답 메시지
 * @param <T> 응답 데이터 타입
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String errorCode,
        String message
) {

    /**
     * 성공 응답을 생성합니다.
     *
     * @param data 응답 데이터
     * @param <T> 응답 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, "OK");
    }

    /**
     * 실패 응답을 생성합니다.
     *
     * @param errorCode 에러 코드
     * @param message 메시지
     * @param <T> 응답 데이터 타입
     * @return 실패 응답
     */
    public static <T> ApiResponse<T> fail(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}

