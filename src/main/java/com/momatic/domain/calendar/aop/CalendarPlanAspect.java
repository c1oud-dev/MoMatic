package com.momatic.domain.calendar.aop;

import com.momatic.domain.plan.service.PlanAccessChecker;
import com.momatic.global.aop.OAuth2PrincipalResolver;
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

    private final PlanAccessChecker planAccessChecker;

    /**
     * Google Calendar 연동 요청자가 Pro 이상 플랜인지 검증합니다.
     *
     * @param joinPoint 조인 포인트
     */
    @Before("@annotation(com.momatic.domain.calendar.aop.CalendarPlanCheck)")
    public void validateCalendarPlan(JoinPoint joinPoint) {
        OAuth2User principal = OAuth2PrincipalResolver.resolve(joinPoint.getArgs());
        planAccessChecker.requireNotFree(principal);
    }
}
