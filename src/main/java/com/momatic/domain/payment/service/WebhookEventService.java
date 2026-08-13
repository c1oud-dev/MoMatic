package com.momatic.domain.payment.service;

import com.momatic.domain.payment.entity.WebhookEvent;
import com.momatic.domain.payment.entity.WebhookEventStatus;
import com.momatic.domain.payment.repository.WebhookEventRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Webhook 처리 트랜잭션과 독립적으로 이벤트 수신 및 처리 결과를 기록합니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private static final int MAX_RETRY_COUNT = 7;

    private final WebhookEventRepository webhookEventRepository;

    /**
     * 동일한 전송 ID의 이벤트가 이미 처리되었는지 확인합니다.
     *
     * @param transmissionId Webhook 고유 전송 ID
     * @return 처리 완료 상태이면 {@code true}
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public boolean isAlreadyProcessed(String transmissionId) {
        return webhookEventRepository.findByTransmissionId(transmissionId)
                .map(event -> event.getStatus() == WebhookEventStatus.PROCESSED)
                .orElse(false);
    }

    /**
     * 신규 Webhook을 기록하거나 재전송된 이벤트를 다시 수신 상태로 엽니다.
     *
     * @param transmissionId Webhook 고유 전송 ID
     * @param eventType 이벤트 유형
     * @param orderId 주문 ID
     * @param payload 원본 요청 본문
     * @param retriedCount 토스페이먼츠 재전송 횟수
     * @return Webhook 이벤트 기록 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long recordReceived(String transmissionId,
                               String eventType,
                               String orderId,
                               String payload,
                               int retriedCount) {
        WebhookEvent event = webhookEventRepository.findByTransmissionId(transmissionId)
                .map(existingEvent -> {
                    existingEvent.reopen(retriedCount);
                    return existingEvent;
                })
                .orElseGet(() -> WebhookEvent.create(
                        transmissionId,
                        eventType,
                        orderId,
                        payload,
                        retriedCount
                ));
        return webhookEventRepository.save(event).getId();
    }

    /**
     * Webhook 이벤트를 처리 완료 상태로 변경합니다.
     *
     * @param webhookEventId Webhook 이벤트 기록 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(Long webhookEventId) {
        findById(webhookEventId).markProcessed();
    }

    /**
     * Webhook 이벤트의 실패 원인과 재전송 횟수를 기록합니다.
     *
     * @param webhookEventId Webhook 이벤트 기록 ID
     * @param errorMessage 실패 원인 메시지
     * @param retriedCount 토스페이먼츠 재전송 횟수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long webhookEventId,
                           String errorMessage,
                           int retriedCount) {
        WebhookEvent event = findById(webhookEventId);
        event.markFailed(errorMessage, retriedCount, MAX_RETRY_COUNT);
        if (event.getStatus() == WebhookEventStatus.GIVEN_UP) {
            log.error(
                    "Webhook 최대 재전송 횟수에 도달하여 수동 확인이 필요합니다: "
                            + "webhookEventId={}, transmissionId={}, retriedCount={}",
                    event.getId(),
                    event.getTransmissionId(),
                    event.getRetriedCount()
            );
        }
    }

    /**
     * Webhook 이벤트 기록을 ID로 조회합니다.
     *
     * @param webhookEventId Webhook 이벤트 기록 ID
     * @return Webhook 이벤트 기록
     */
    private WebhookEvent findById(Long webhookEventId) {
        return webhookEventRepository.findById(webhookEventId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }
}