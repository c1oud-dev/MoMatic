package com.momatic.domain.plan.entity;

import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;

import java.math.BigDecimal;
import java.util.Arrays;

/** 구독 플랜별 업로드 제한 정책을 정의하는 열거형입니다. */
public enum PlanPolicy {
    FREE(3L, 26_214_400L, 0L),
    PRO(200L, 524_288_000L, 19_900L),
    TEAM(1_000L, 1_073_741_824L, 49_900L);

    private final long monthlyUploadCount;
    private final long maxFileSizeBytes;
    private final BigDecimal price;

    /**
     * 플랜 정책을 생성합니다.
     *
     * @param monthlyUploadCount 월 업로드 가능 횟수
     * @param maxFileSizeBytes 파일당 최대 업로드 바이트 수
     * @param price 월 결제 금액
     */
    PlanPolicy(long monthlyUploadCount,
               long maxFileSizeBytes,
               long price) {
        this.monthlyUploadCount = monthlyUploadCount;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.price = BigDecimal.valueOf(price);
    }

    /**
     * 문자열에 해당하는 플랜 정책을 조회합니다.
     *
     * @param planType 플랜 타입 문자열
     * @return 플랜 정책
     */
    public static PlanPolicy from(String planType) {
        if (planType == null) {
            throw new CustomException(ErrorCode.INVALID_PLAN_TYPE);
        }

        return Arrays.stream(values())
                .filter(policy -> policy.name().equalsIgnoreCase(planType))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PLAN_TYPE));
    }

    /**
     * 월 업로드 가능 횟수를 조회합니다.
     *
     * @return 월 업로드 가능 횟수
     */
    public long getMonthlyUploadCount() {
        return monthlyUploadCount;
    }

    /**
     * 파일당 최대 업로드 바이트 수를 조회합니다.
     *
     * @return 파일당 최대 업로드 바이트 수
     */
    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    /**
     * 월 결제 금액을 조회합니다.
     *
     * @return 월 결제 금액
     */
    public BigDecimal getPrice() {
        return price;
    }
}

