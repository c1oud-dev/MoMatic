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
        PlanPolicy planPolicy = getActivePlan(principal);
        if (planPolicy == PlanPolicy.FREE) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 인증 사용자가 Google Calendar 신규 일정을 등록할 수 있는지 검증합니다.
     *
     * @param principal OAuth2 인증 사용자 정보
     */
    public void requireCalendarAccess(OAuth2User principal) {
        if (!isCalendarAvailable(principal)) {
            throw new CustomException(ErrorCode.CALENDAR_PLAN_REQUIRED);
        }
    }

    /**
     * 인증 사용자가 Google Calendar 신규 일정을 등록할 수 있는지 확인합니다.
     *
     * @param principal OAuth2 인증 사용자 정보
     * @return 신규 일정 등록 가능 여부
     */
    public boolean isCalendarAvailable(OAuth2User principal) {
        return getActivePlan(principal).isCalendarAvailable();
    }

    /**
     * OAuth2 인증 사용자의 활성 플랜을 조회합니다.
     *
     * @param principal OAuth2 인증 사용자 정보
     * @return 활성 플랜
     */
    private PlanPolicy getActivePlan(OAuth2User principal) {
        User user = userRepository.findByEmail(principal.getAttribute("email"))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return subscriptionService.getActivePlan(user.getId());
    }
}