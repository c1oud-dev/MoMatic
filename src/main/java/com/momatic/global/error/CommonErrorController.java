package com.momatic.global.error;

import com.momatic.global.api.ApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** 서블릿 컨테이너 오류를 공통 화면 또는 API 응답으로 변환합니다. */
@Controller
public class CommonErrorController implements ErrorController {

    /**
     * 상태 코드에 맞는 공통 오류 응답을 반환합니다.
     *
     * @param request HTTP 요청
     * @param model 화면 모델
     * @return 공통 오류 화면 또는 JSON 응답
     */
    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        ErrorCode errorCode = resolveErrorCode(request);
        if (RequestTypeResolver.isAjaxRequest(request)) {
            return jsonError(errorCode);
        }

        model.addAttribute(
                "errorCode",
                resolveAttribute(request, "errorCode", errorCode.name()));
        model.addAttribute(
                "errorMessage",
                resolveAttribute(request, "errorMessage", errorCode.getMessage()));
        return "error/common";
    }

    /**
     * 오류 코드를 공통 API 응답으로 변환합니다.
     *
     * @param errorCode 오류 코드
     * @return JSON 오류 응답
     */
    @ResponseBody
    private ResponseEntity<ApiResponse<Void>> jsonError(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
    }

    /**
     * 요청의 HTTP 상태에 해당하는 오류 코드를 결정합니다.
     *
     * @param request HTTP 요청
     * @return 오류 코드
     */
    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttribute instanceof Integer
                ? (Integer) statusAttribute
                : HttpStatus.INTERNAL_SERVER_ERROR.value();
        return Map.of(
                        HttpStatus.UNAUTHORIZED.value(), ErrorCode.UNAUTHORIZED,
                        HttpStatus.FORBIDDEN.value(), ErrorCode.FORBIDDEN,
                        HttpStatus.NOT_FOUND.value(), ErrorCode.NOT_FOUND)
                .getOrDefault(status, ErrorCode.INTERNAL_ERROR);
    }

    /**
     * 명시적으로 전달된 오류 속성 또는 기본값을 반환합니다.
     *
     * @param request HTTP 요청
     * @param name 속성 이름
     * @param defaultValue 기본값
     * @return 오류 표시 값
     */
    private String resolveAttribute(HttpServletRequest request,
                                    String name,
                                    String defaultValue) {
        Object value = request.getAttribute(name);
        return value instanceof String ? (String) value : defaultValue;
    }
}