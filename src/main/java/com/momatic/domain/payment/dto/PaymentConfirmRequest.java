package com.momatic.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청 DTO입니다.
 *
 * @param orderId 주문 ID
 * @param paymentKey 토스페이먼츠 결제 키
 * @param amount 결제 금액
 */
public record PaymentConfirmRequest(
        @NotBlank String orderId,
        @NotBlank String paymentKey,
        @NotNull @Positive BigDecimal amount
) {
}
