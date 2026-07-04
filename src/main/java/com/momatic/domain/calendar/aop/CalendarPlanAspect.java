package com.momatic.domain.calendar.aop;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/** Google Calendar 연동 요청의 플랜 제한을 검증하는 AOP 클래스입니다. */
@Aspect
@Component
@RequiredArgsConstructor
public class CalendarPlanAspect {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    /**
     * Google Calendar 연동 요청자가 Pro 이상 플랜인지 검증합니다.
     *
     * @param joinPoint 조인 포인트
     */
    @Before("@annotation(com.momatic.domain.calendar.aop.CalendarPlanCheck)")
    public void validateCalendarPlan(JoinPoint joinPoint) {
        OAuth2User principal = findPrincipal(joinPoint.getArgs());
        User user = userRepository.findByEmail(principal.getAttribute("email"))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        PlanPolicy planPolicy = subscriptionService.getActivePlan(user.getId());
        if (planPolicy == PlanPolicy.FREE) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 메서드 인자에서 인증 사용자 정보를 조회합니다.
     *
     * @param args 메서드 인자
     * @return 인증 사용자 정보
     */
    private OAuth2User findPrincipal(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof OAuth2User principal) {
                return principal;
            }
        }
        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
}
