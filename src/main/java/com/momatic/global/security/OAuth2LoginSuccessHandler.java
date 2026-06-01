package com.momatic.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** OAuth2 로그인 성공 시 리다이렉트를 처리하는 핸들러입니다. */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    /** 로그인 성공 후 대시보드로 이동시킵니다. */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.sendRedirect("/dashboard");
    }
}
