package com.momatic.domain.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/** Google OIDC 사용자 정보를 동기화하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final UserSyncService userSyncService;

    /**
     * OIDC 사용자 요청을 처리하고 로컬 사용자 정보를 갱신합니다.
     *
     * @param userRequest OIDC 사용자 요청
     * @return 애플리케이션에서 사용할 OIDC 사용자 객체
     * @throws OAuth2AuthenticationException 인증 처리 중 오류가 발생한 경우
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        userSyncService.syncUser(email, name);
        return oidcUser;
    }
}