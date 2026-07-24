package com.momatic.global.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

/** 인증 사용자 정보에서 공통 인증 속성을 조회하는 유틸리티입니다. */
public final class AuthenticatedUserResolver {

    /** 유틸리티 클래스 인스턴스 생성을 방지합니다. */
    private AuthenticatedUserResolver() {
    }

    /**
     * 인증 사용자 정보에서 이메일을 조회합니다.
     *
     * @param principal 인증 사용자 정보
     * @return 인증 사용자 이메일
     */
    public static String getEmail(OAuth2User principal) {
        return principal.getAttribute("email");
    }
}
