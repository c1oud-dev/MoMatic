package com.momatic.domain.admin.dto;

import com.momatic.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;

/**
 * 관리자 구독 현황 화면에 표시할 구독 응답 DTO입니다.
 *
 * @param id 구독 ID
 * @param userEmail 사용자 이메일
 * @param planType 플랜 타입
 * @param status 구독 상태
 * @param startedAt 구독 시작 일시
 * @param expiredAt 구독 만료 일시
 */
public record AdminSubscriptionResponse(
        Long id,
        String userEmail,
        String planType,
        String status,
        LocalDateTime startedAt,
        LocalDateTime expiredAt
) {

    /**
     * 구독 엔티티로 관리자 구독 응답 DTO를 생성합니다.
     *
     * @param subscription 구독 엔티티
     * @return 관리자 구독 응답 DTO
     */
    public static AdminSubscriptionResponse from(Subscription subscription) {
        return new AdminSubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getEmail(),
                subscription.getPlanType().name(),
                subscription.getStatus().name(),
                subscription.getStartedAt(),
                subscription.getExpiredAt()
        );
    }
}