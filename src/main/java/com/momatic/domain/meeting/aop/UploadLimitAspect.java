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
import org.springframework.web.multipart.MultipartFile;

/** 업로드 횟수와 파일 크기 제한을 공통 검증하는 AOP 클래스입니다. */
@Aspect
@Component
@RequiredArgsConstructor
public class UploadLimitAspect {

    private static final String USAGE_TYPE_UPLOAD = "UPLOAD";

    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 업로드 요청의 월간 횟수 및 파일 크기 제한을 검증합니다.
     *
     * @param joinPoint 조인 포인트
     */
    @Before("@annotation(com.momatic.domain.meeting.aop.UploadLimitCheck)")
    public void validateUploadLimit(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        PlanPolicy planPolicy = PlanPolicy.from(subscriptionService.getActivePlan(userId).name());
        MultipartFile file = findMultipartFile(args);
        validateFileSize(file, planPolicy);
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

    /**
     * 업로드 인자에서 파일을 조회합니다.
     *
     * @param args 업로드 메서드 인자
     * @return 업로드 파일
     */
    private MultipartFile findMultipartFile(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof MultipartFile multipartFile) {
                return multipartFile;
            }
        }
        throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    /**
     * 플랜별 파일 크기 제한을 검증합니다.
     *
     * @param file 업로드 파일
     * @param planPolicy 플랜 정책
     */
    private void validateFileSize(MultipartFile file,
                                  PlanPolicy planPolicy) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (file.getSize() > planPolicy.getMaxFileSizeBytes()) {
            throw new CustomException(ErrorCode.UPLOAD_FILE_SIZE_EXCEEDED);
        }
    }
}