package com.momatic.domain.meeting.aop;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.usage.repository.UsageRecordRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/** 업로드 월 횟수 제한을 검증하는 AOP 클래스입니다. */
@Aspect
@Component
@RequiredArgsConstructor
public class UploadLimitAspect {

    private static final String USAGE_TYPE_UPLOAD = "UPLOAD";

    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 업로드 요청의 월간 횟수 제한을 검증합니다.
     *
     * @param joinPoint 조인 포인트
     */
    @Before("@annotation(com.momatic.domain.meeting.aop.UploadLimitCheck)")
    public void validateUploadLimit(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        PlanPolicy planPolicy = subscriptionService.getActivePlan(userId);
        long limit = planPolicy.getMonthlyUploadCount();
        LocalDateTime start = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime end = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
        long usageCount = usageRecordRepository
                .countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        userId,
                        USAGE_TYPE_UPLOAD,
                        start,
                        end
                );

        if (usageCount >= limit) {
            throw new CustomException(ErrorCode.UPLOAD_MONTHLY_LIMIT_EXCEEDED);
        }
    }
}