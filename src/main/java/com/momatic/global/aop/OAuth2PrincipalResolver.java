package com.momatic.global.aop;

import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import org.springframework.security.oauth2.core.user.OAuth2User;

/** AOP 조인 포인트 인자에서 OAuth2 인증 사용자 정보를 조회하는 유틸리티 클래스입니다. */
public final class OAuth2PrincipalResolver {

    /** OAuth2 인증 사용자 조회 유틸리티 인스턴스 생성을 방지합니다. */
    private OAuth2PrincipalResolver() {
    }

    /**
     * 메서드 인자에서 인증 사용자 정보를 조회합니다.
     *
     * @param args 메서드 인자
     * @return 인증 사용자 정보
     */
    public static OAuth2User resolve(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof OAuth2User principal) {
                return principal;
            }
        }
        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
}