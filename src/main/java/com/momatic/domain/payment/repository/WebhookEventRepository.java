package com.momatic.domain.payment.repository;

import com.momatic.domain.payment.entity.WebhookEvent;
import com.momatic.domain.payment.entity.WebhookEventStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Webhook 이벤트 처리 이력을 저장하고 조회하는 레포지토리입니다. */
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    /**
     * 전송 ID에 해당하는 Webhook 이벤트를 조회합니다.
     *
     * @param transmissionId Webhook 고유 전송 ID
     * @return Webhook 이벤트 처리 이력
     */
    Optional<WebhookEvent> findByTransmissionId(String transmissionId);

    /**
     * 처리 상태에 해당하는 Webhook 이벤트를 조회합니다.
     *
     * @param status 조회할 처리 상태
     * @return Webhook 이벤트 처리 이력 목록
     */
    List<WebhookEvent> findAllByStatus(WebhookEventStatus status);
}
