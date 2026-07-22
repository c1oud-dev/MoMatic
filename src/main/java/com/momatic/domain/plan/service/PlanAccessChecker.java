package com.momatic.domain.plan.service;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/** OAuth2 인증 사용자의 플랜 접근 권한을 검증하는 컴포넌트입니다. */
@Component
@RequiredArgsConstructor
public class PlanAccessChecker {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    /**
     * 인증 사용자가 무료 플랜이 아닌지 검증합니다.
     *
     * @param principal OAuth2 인증 사용자 정보
     */
    public void requireNotFree(OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email"))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        PlanPolicy planPolicy = subscriptionService.getActivePlan(user.getId());
        if (planPolicy == PlanPolicy.FREE) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}