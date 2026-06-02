package com.momatic.domain.payment.dto;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청 DTO입니다.
 *
 * @param orderId 주문 ID
 * @param paymentKey 토스페이먼츠 결제 키
 * @param amount 결제 금액
 */
public record PaymentConfirmRequest(
        String orderId,
        String paymentKey,
        BigDecimal amount
) {
}
