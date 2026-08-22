package com.momatic.global.security;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.domain.user.service.UserSyncService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Locale;
import java.util.Map;

/**
 * 개발 환경에서 OAuth2 로그인 없이 DB 사용자를 인증하는 필터입니다.
 * {@code mockUser} 쿼리 파라미터에 이메일을 지정하면 해당 사용자로 전환하며, 선택한 이메일은
 * HTTP 세션에 보관됩니다. 이후 요청은 파라미터 없이 같은 사용자로 처리되고, 다른 이메일을 다시
 * 지정하면 즉시 전환됩니다. 파라미터와 세션 값이 모두 없으면 {@code dev@momatic.com}을 사용합니다.
 *
 * 이 필터는 {@code dev} 프로파일에서만 빈으로 등록됩니다.
 */
@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class MockAuthenticationFilter extends OncePerRequestFilter {

    private static final String MOCK_USER_PARAMETER = "mockUser";
    private static final String MOCK_USER_SESSION_ATTRIBUTE = "MOMATIC_MOCK_USER_EMAIL";
    private static final String DEFAULT_EMAIL = "dev@momatic.com";
    private static final String DEFAULT_NAME = "개발자";

    private final UserRepository userRepository;
    private final UserSyncService userSyncService;

    /** 필터가 개발 환경에서 활성화되었음을 시작 로그에 기록합니다. */
    @PostConstruct
    public void logActivation() {
        log.warn("MockAuthenticationFilter is active (dev profile only, parameter={})",
                MOCK_USER_PARAMETER);
    }

    /**
     * 요청 또는 세션에서 Mock 사용자 이메일을 결정하고 DB 권한으로 인증합니다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 다음 필터 처리 중 오류가 발생한 경우
     * @throws IOException 다음 필터의 입출력 처리 중 오류가 발생한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String email = resolveEmail(request);
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userSyncService.syncUser(email, mockName(email)));

        Map<String, Object> attributes = Map.of(
                "email", user.getEmail(),
                "name", user.getName(),
                "sub", "mock-" + user.getEmail()
        );
        DefaultOAuth2User mockUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole())),
                attributes,
                "email"
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    /**
     * 쿼리 파라미터, 세션, 기본값 순서로 사용할 이메일을 결정합니다.
     *
     * @param request HTTP 요청
     * @return 정규화된 Mock 사용자 이메일
     */
    private String resolveEmail(HttpServletRequest request) {
        String requestedEmail = request.getParameter(MOCK_USER_PARAMETER);
        if (requestedEmail != null && !requestedEmail.isBlank()) {
            String normalizedEmail = requestedEmail.trim().toLowerCase(Locale.ROOT);
            request.getSession(true).setAttribute(MOCK_USER_SESSION_ATTRIBUTE, normalizedEmail);
            return normalizedEmail;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionEmail = session.getAttribute(MOCK_USER_SESSION_ATTRIBUTE);
            if (sessionEmail instanceof String email && !email.isBlank()) {
                return email;
            }
        }
        return DEFAULT_EMAIL;
    }

    /**
     * 자동 생성할 Mock 사용자의 표시 이름을 만듭니다.
     *
     * @param email 사용자 이메일
     * @return Mock 사용자 표시 이름
     */
    private String mockName(String email) {
        int domainSeparator = email.indexOf('@');
        String identifier = domainSeparator > 0 ? email.substring(0, domainSeparator) : email;
        return DEFAULT_EMAIL.equals(email) ? DEFAULT_NAME : "Mock " + identifier;
    }
}
