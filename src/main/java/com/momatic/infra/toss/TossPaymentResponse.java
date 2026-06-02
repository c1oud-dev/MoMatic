package com.momatic.infra.toss;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 승인 API 응답 DTO입니다.
 *
 * @param orderId 주문 ID
 * @param paymentKey 토스페이먼츠 결제 키
 * @param totalAmount 총 결제 금액
 * @param status 결제 상태
 */
public record TossPaymentResponse(
        String orderId,
        String paymentKey,
        BigDecimal totalAmount,
        String status
) {
}