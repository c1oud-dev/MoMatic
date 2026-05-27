package com.momatic.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 로그아웃 성공 시 후처리를 담당하는 핸들러입니다. */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    /** 로그아웃 후 로그인 페이지로 이동합니다. */
    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                final Authentication authentication) throws IOException, ServletException {
        response.sendRedirect("/login");
    }
}
