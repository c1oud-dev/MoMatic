package com.momatic.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Map;

/**
 * 개발 환경에서 OAuth2 로그인 없이 테스트할 수 있도록 고정 사용자를 SecurityContext에 주입하는 필터입니다.
 * dev 프로파일에서만 동작합니다.
 */
@Profile("dev")
@Component
public class MockAuthenticationFilter extends OncePerRequestFilter {

    private static final String MOCK_EMAIL = "dev@momatic.com";
    private static final String MOCK_NAME = "개발자";

    /**
     * 요청마다 고정된 Mock 사용자를 SecurityContext에 주입합니다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Map<String, Object> attributes = Map.of(
                    "email", MOCK_EMAIL,
                    "name", MOCK_NAME,
                    "sub", "mock-sub-id"
            );

            DefaultOAuth2User mockUser = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email"
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
