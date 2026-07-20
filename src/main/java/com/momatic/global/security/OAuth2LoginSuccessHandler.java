package com.momatic.global.security;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** OAuth2 로그인 성공 시 Google 토큰 저장과 리다이렉트를 처리하는 핸들러입니다. */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;

    /**
     * 로그인 성공 시 Google OAuth2 토큰을 저장하고 대시보드로 이동시킵니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 정보
     * @throws IOException 리다이렉트 처리 중 오류가 발생한 경우
     * @throws ServletException 인증 성공 처리 중 오류가 발생한 경우
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        saveGoogleToken(authentication);
        setDefaultTargetUrl("/dashboard");
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 인증 정보에 연결된 Google OAuth2 토큰을 사용자 엔티티에 저장합니다.
     *
     * @param authentication 인증 정보
     */
    private void saveGoogleToken(Authentication authentication) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                GOOGLE_REGISTRATION_ID,
                authentication.getName()
        );
        if (authorizedClient == null) {
            return;
        }

        OAuth2User oAuth2User =
                (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            return;
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        String refreshTokenValue = refreshToken == null ? null : refreshToken.getTokenValue();
        LocalDateTime expiresAt = accessToken.getExpiresAt() == null
                ? null
                : LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault());

        userRepository.findByEmail(email)
                .ifPresent(user -> updateGoogleToken(user, accessToken.getTokenValue(), refreshTokenValue, expiresAt));
    }

    /**
     * 사용자 엔티티의 Google OAuth2 토큰 정보를 갱신합니다.
     *
     * @param user 사용자 엔티티
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresAt 액세스 토큰 만료 시각
     */
    private void updateGoogleToken(User user,
                                   String accessToken,
                                   String refreshToken,
                                   LocalDateTime expiresAt) {
        user.updateGoogleToken(accessToken, refreshToken, expiresAt);
    }
}
