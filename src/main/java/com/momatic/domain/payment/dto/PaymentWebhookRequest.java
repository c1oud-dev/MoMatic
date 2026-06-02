package com.momatic.domain.payment.dto;

/**
 * 토스페이먼츠 Webhook 요청 DTO입니다.
 *
 * @param eventType 이벤트 타입
 * @param data 결제 이벤트 데이터
 */
public record PaymentWebhookRequest(
        String eventType,
        PaymentWebhookData data
) {

    /**
     * 토스페이먼츠 Webhook 결제 데이터입니다.
     *
     * @param orderId 주문 ID
     * @param paymentKey 토스페이먼츠 결제 키
     */
    public record PaymentWebhookData(
            String orderId,
            String paymentKey
    ) {
    }
}
