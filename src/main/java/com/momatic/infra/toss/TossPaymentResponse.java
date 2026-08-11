package com.momatic.infra.toss;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 객체 DTO입니다.
 * 결제 승인 API 응답과 결제 상태 변경 Webhook의 data 객체에 함께 사용됩니다.
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