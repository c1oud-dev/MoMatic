package com.momatic.domain.payment.dto;

import com.momatic.domain.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 처리 결과 DTO입니다.
 *
 * @param orderId 주문 ID
 * @param amount 결제 금액
 * @param planType 플랜 타입
 * @param status 결제 상태
 * @param createdAt 결제 생성 시각
 */
public record PaymentResponse(
        String orderId,
        BigDecimal amount,
        String planType,
        String status,
        LocalDateTime createdAt
) {

    /**
     * 결제 엔티티를 응답 DTO로 변환합니다.
     *
     * @param payment 결제 엔티티
     * @return 결제 응답 DTO
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPlanType().name(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}
