package com.momatic.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.error.ErrorCode;
import com.momatic.global.error.RequestTypeResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

/** 인증 및 인가 실패를 요청 유형에 맞게 응답하는 보안 오류 핸들러입니다. */
@Component
public class SecurityErrorHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final String LOGIN_URL = "/oauth2/authorization/google";

    private final ObjectMapper objectMapper;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * 보안 오류 핸들러를 생성합니다.
     *
     * @param objectMapper JSON 직렬화 도구
     */
    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 접근 거부 응답을 공통 오류 화면 또는 JSON으로 반환합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param accessDeniedException 접근 거부 예외
     * @throws IOException 응답 처리 실패 시 발생
     * @throws ServletException 포워드 실패 시 발생
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (RequestTypeResolver.isAjaxRequest(request)) {
            writeJsonError(response, ErrorCode.FORBIDDEN);
            return;
        }

        forwardToCommonError(request, response, ErrorCode.FORBIDDEN);
    }

    /**
     * 미인증 응답을 로그인 절차 또는 JSON으로 반환합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authenticationException 인증 예외
     * @throws IOException 응답 처리 실패 시 발생
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        if (RequestTypeResolver.isAjaxRequest(request)) {
            writeJsonError(response, ErrorCode.UNAUTHORIZED);
            return;
        }

        redirectStrategy.sendRedirect(request, response, LOGIN_URL);
    }

    /**
     * 보안 오류를 공통 오류 엔드포인트로 전달합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param errorCode 오류 코드
     * @throws IOException 포워드 응답 실패 시 발생
     * @throws ServletException 포워드 실패 시 발생
     */
    private void forwardToCommonError(HttpServletRequest request,
                                      HttpServletResponse response,
                                      ErrorCode errorCode) throws IOException, ServletException {
        response.setStatus(errorCode.getStatus().value());
        request.setAttribute("errorCode", errorCode.name());
        request.setAttribute("errorMessage", errorCode.getMessage());
        request.getRequestDispatcher("/error").forward(request, response);
    }

    /**
     * 보안 오류를 공통 API 응답 형식으로 작성합니다.
     *
     * @param response HTTP 응답
     * @param errorCode 오류 코드
     * @throws IOException JSON 응답 작성 실패 시 발생
     */
    private void writeJsonError(HttpServletResponse response,
                                ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
    }
}