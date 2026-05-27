package com.momatic.global.error;

import com.momatic.global.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 페이지 요청과 AJAX 요청을 분기 처리하는 전역 예외 핸들러입니다.
 */
@Controller
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외를 요청 유형에 따라 처리합니다.
     *
     * @param exception 커스텀 예외
     * @param request HTTP 요청
     * @return 페이지 뷰 이름 혹은 JSON 응답
     */
    @ExceptionHandler(CustomException.class)
    public Object handleCustomException(final CustomException exception,
                                        final HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return handleAjax(exception);
        }

        request.setAttribute("errorCode", exception.getErrorCode().name());
        request.setAttribute("errorMessage", exception.getMessage());
        return "error/common";
    }

    /**
     * 그 외 예외를 요청 유형에 따라 처리합니다.
     *
     * @param exception 예외
     * @param request HTTP 요청
     * @return 페이지 뷰 이름 혹은 JSON 응답
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(final Exception exception,
                                  final HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.name(), ErrorCode.INTERNAL_ERROR.getMessage()));
        }

        request.setAttribute("errorCode", ErrorCode.INTERNAL_ERROR.name());
        request.setAttribute("errorMessage", ErrorCode.INTERNAL_ERROR.getMessage());
        return "error/common";
    }

    @ResponseBody
    private ResponseEntity<ApiResponse<Void>> handleAjax(final CustomException exception) {
        final ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
    }

    private boolean isAjaxRequest(final HttpServletRequest request) {
        final String requestedWith = request.getHeader("X-Requested-With");
        final String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}

