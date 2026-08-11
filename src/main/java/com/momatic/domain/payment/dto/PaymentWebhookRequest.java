package com.momatic.domain.payment.dto;

import com.momatic.infra.toss.TossPaymentResponse;

/**
 * 토스페이먼츠 Webhook 요청 DTO입니다.
 *
 * @param eventType 이벤트 타입
 * @param createdAt 이벤트 생성 시각
 * @param data 승인 API 응답과 동일한 결제 객체
 */
public record PaymentWebhookRequest(
        String eventType,
        String createdAt,
        TossPaymentResponse data
) {
}
