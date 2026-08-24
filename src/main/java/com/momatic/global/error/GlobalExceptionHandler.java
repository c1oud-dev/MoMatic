package com.momatic.global.error;

import com.momatic.global.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 페이지 요청과 AJAX 요청을 분기 처리하는 전역 예외 핸들러입니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외를 요청 유형에 따라 처리합니다.
     *
     * @param exception 커스텀 예외
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 페이지 뷰 이름 혹은 JSON 응답
     */
    @ExceptionHandler(CustomException.class)
    public Object handleCustomException(CustomException exception,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        if (RequestTypeResolver.isAjaxRequest(request)) {
            return handleAjax(exception);
        }
        if (isPlanUpgradeRequired(exception.getErrorCode())) {
            return "redirect:/plans";
        }

        request.setAttribute("errorCode", exception.getErrorCode().name());
        request.setAttribute("errorMessage", exception.getMessage());
        response.setStatus(exception.getErrorCode().getStatus().value());
        return "error/common";
    }

    /**
     * 그 외 예외를 요청 유형에 따라 처리합니다.
     *
     * @param exception 예외
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 페이지 뷰 이름 혹은 JSON 응답
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        if (RequestTypeResolver.isAjaxRequest(request)) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.name(), ErrorCode.INTERNAL_ERROR.getMessage()));
        }

        request.setAttribute("errorCode", ErrorCode.INTERNAL_ERROR.name());
        request.setAttribute("errorMessage", ErrorCode.INTERNAL_ERROR.getMessage());
        response.setStatus(ErrorCode.INTERNAL_ERROR.getStatus().value());
        return "error/common";
    }

    /**
     * 커스텀 예외를 AJAX JSON 응답으로 변환합니다.
     *
     * @param exception 커스텀 예외
     * @return JSON 예외 응답
     */
    @ResponseBody
    private ResponseEntity<ApiResponse<Void>> handleAjax(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
    }

    /**
     * 업로드 차단 예외가 요금제 업그레이드 안내 대상인지 확인합니다.
     *
     * @param errorCode 에러 코드
     * @return 요금제 업그레이드 안내 대상 여부
     */
    private boolean isPlanUpgradeRequired(ErrorCode errorCode) {
        return errorCode == ErrorCode.UPLOAD_MONTHLY_LIMIT_EXCEEDED
                || errorCode == ErrorCode.UPLOAD_FILE_SIZE_EXCEEDED;
    }
}