package com.momatic.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.momatic.domain.payment.entity.WebhookEvent;
import com.momatic.domain.payment.entity.WebhookEventStatus;
import com.momatic.domain.payment.repository.WebhookEventRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Webhook 이벤트 기록 서비스의 상태 변경 동작을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class WebhookEventServiceTest {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @InjectMocks
    private WebhookEventService webhookEventService;

    /** 재전송 횟수가 최대치에 도달한 실패를 포기 상태로 기록하는지 검증합니다. */
    @Test
    @DisplayName("재전송 횟수가 7 이상이면서 실패하면 GIVEN_UP으로 변경된다")
    void markFailedChangesStatusToGivenUpAtMaximumRetryCount() {
        // given
        WebhookEvent event = WebhookEvent.create(
                "transmission-1",
                "PAYMENT_STATUS_CHANGED",
                "order-1",
                "{}",
                7
        );
        ReflectionTestUtils.setField(event, "id", 1L);
        when(webhookEventRepository.findById(1L)).thenReturn(Optional.of(event));

        // when
        webhookEventService.markFailed(1L, "처리 실패", 7);

        // then
        assertEquals(WebhookEventStatus.GIVEN_UP, event.getStatus());
        assertEquals("처리 실패", event.getLastErrorMessage());
        assertEquals(7, event.getRetriedCount());
    }
}