package com.momatic.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 토스페이먼츠 Webhook의 수신 및 처리 이력을 기록하는 엔티티입니다. */
@Entity
@Table(name = "webhook_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String transmissionId;

    @Column(length = 50)
    private String eventType;

    @Column(length = 100)
    private String orderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WebhookEventStatus status;

    @Column(nullable = false)
    private int retriedCount;

    @Column(length = MAX_ERROR_MESSAGE_LENGTH)
    private String lastErrorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    /**
     * Webhook 수신 기록을 생성합니다.
     *
     * @param transmissionId Webhook 고유 전송 ID
     * @param eventType 이벤트 유형
     * @param orderId 주문 ID
     * @param payload 원본 요청 본문
     * @param retriedCount 토스페이먼츠 재전송 횟수
     * @return 수신 상태의 Webhook 기록
     */
    public static WebhookEvent create(String transmissionId,
                                      String eventType,
                                      String orderId,
                                      String payload,
                                      int retriedCount) {
        WebhookEvent event = new WebhookEvent();
        event.transmissionId = transmissionId;
        event.eventType = eventType;
        event.orderId = orderId;
        event.payload = payload;
        event.status = WebhookEventStatus.RECEIVED;
        event.retriedCount = retriedCount;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    /** 이벤트 처리를 완료하고 처리 완료 시각을 기록합니다. */
    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 이벤트 처리 실패 원인과 재전송 횟수를 기록합니다.
     *
     * @param errorMessage 실패 원인 메시지
     * @param retriedCount 토스페이먼츠 재전송 횟수
     * @param maxRetryCount 최대 재전송 횟수
     */
    public void markFailed(String errorMessage,
                           int retriedCount,
                           int maxRetryCount) {
        this.status = retriedCount >= maxRetryCount
                ? WebhookEventStatus.GIVEN_UP
                : WebhookEventStatus.FAILED;
        this.lastErrorMessage = normalizeErrorMessage(errorMessage);
        this.retriedCount = retriedCount;
    }

    /**
     * 재전송된 이벤트를 다시 수신 상태로 변경합니다.
     *
     * @param retriedCount 토스페이먼츠 재전송 횟수
     */
    public void reopen(int retriedCount) {
        this.status = WebhookEventStatus.RECEIVED;
        this.retriedCount = retriedCount;
        this.lastErrorMessage = null;
        this.processedAt = null;
    }

    /**
     * 데이터베이스 컬럼 길이에 맞게 실패 원인 메시지를 정규화합니다.
     *
     * @param errorMessage 실패 원인 메시지
     * @return 저장 가능한 실패 원인 메시지
     */
    private static String normalizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}