package com.momatic.domain.subscription.dto;

import com.momatic.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;

/**
 * 구독 현황 화면에 표시할 현재 구독 요약 DTO입니다.
 *
 * @param planType 현재 플랜 타입
 * @param expiredAt 구독 만료 시각
 */
public record SubscriptionSummary(
        String planType,
        LocalDateTime expiredAt
) {

    /**
     * 구독 엔티티를 구독 요약 DTO로 변환합니다.
     *
     * @param subscription 구독 엔티티
     * @return 구독 요약 DTO
     */
    public static SubscriptionSummary from(Subscription subscription) {
        return new SubscriptionSummary(
                subscription.getPlanType().name(),
                subscription.getExpiredAt()
        );
    }

    /**
     * 무료 플랜 구독 요약 DTO를 생성합니다.
     *
     * @return 무료 플랜 구독 요약 DTO
     */
    public static SubscriptionSummary free() {
        return new SubscriptionSummary("FREE", null);
    }
}
