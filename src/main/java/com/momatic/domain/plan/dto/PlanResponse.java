package com.momatic.domain.plan.dto;

import com.momatic.domain.plan.entity.PlanPolicy;

/**
 * 요금제 화면에 표시할 플랜 정책 DTO입니다.
 *
 * @param planType 플랜 타입
 * @param monthlyUploadCount 월 업로드 가능 횟수
 * @param maxFileSizeBytes 파일당 최대 업로드 바이트 수
 */
public record PlanResponse(
        String planType,
        long monthlyUploadCount,
        long maxFileSizeBytes
) {
    /**
     * 플랜 정책을 요금제 화면 DTO로 변환합니다.
     *
     * @param planPolicy 플랜 정책
     * @return 요금제 화면 DTO
     */
    public static PlanResponse from(PlanPolicy planPolicy) {
        return new PlanResponse(
                planPolicy.name(),
                planPolicy.getMonthlyUploadCount(),
                planPolicy.getMaxFileSizeBytes()
        );
    }
}
