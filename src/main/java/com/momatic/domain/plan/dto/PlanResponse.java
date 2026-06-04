package com.momatic.domain.plan.dto;

import com.momatic.domain.plan.entity.PlanPolicy;

import java.math.BigDecimal;

/**
 * 요금제 화면에 표시할 플랜 정책 DTO입니다.
 *
 * @param planType 플랜 타입
 * @param monthlyUploadCount 월 업로드 가능 횟수
 * @param maxFileSizeBytes 파일당 최대 업로드 바이트 수
 * @param price 월 결제 금액
 * @param maxFileSizeLabel 파일당 최대 업로드 크기 표시 문자열
 */
public record PlanResponse(
        String planType,
        long monthlyUploadCount,
        long maxFileSizeBytes,
        String maxFileSizeLabel,
        BigDecimal price
) {

    private static final long BYTES_PER_MEGABYTE = 1_048_576L;
    private static final long BYTES_PER_GIGABYTE = 1_073_741_824L;

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
                planPolicy.getMaxFileSizeBytes(),
                formatFileSize(planPolicy.getMaxFileSizeBytes()),
                planPolicy.getPrice()
        );
    }

    /**
     * 바이트 단위 파일 크기를 화면 표시용 문자열로 변환합니다.
     *
     * @param bytes 파일 크기 바이트 수
     * @return 화면 표시용 파일 크기 문자열
     */
    private static String formatFileSize(long bytes) {
        if (bytes >= BYTES_PER_GIGABYTE && bytes % BYTES_PER_GIGABYTE == 0) {
            return bytes / BYTES_PER_GIGABYTE + "GB";
        }
        return bytes / BYTES_PER_MEGABYTE + "MB";
    }
}
