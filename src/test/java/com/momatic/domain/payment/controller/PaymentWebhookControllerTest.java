package com.momatic.domain.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.payment.dto.PaymentWebhookRequest;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.domain.payment.service.WebhookEventService;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.toss.TossPaymentClient;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/** 결제 Webhook 컨트롤러의 이벤트 단위 멱등성 처리를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    private static final String PAYLOAD = """
            {
              "eventType": "PAYMENT_STATUS_CHANGED",
              "createdAt": "2026-08-13T00:00:00Z",
              "data": {
                "orderId": "order-1",
                "paymentKey": "payment-key-1",
                "totalAmount": 19900,
                "status": "DONE"
              }
            }
            """;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    private ObjectMapper objectMapper;

    @Mock
    private WebhookEventService webhookEventService;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private PaymentWebhookController paymentWebhookController;

    /** 각 테스트에서 실제 JSON 파싱에 사용할 객체 매퍼를 설정합니다. */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        paymentWebhookController = new PaymentWebhookController(
                paymentService,
                tossPaymentClient,
                objectMapper,
                webhookEventService
        );
        when(tossPaymentClient.isValidWebhookAuthorization("Basic valid"))
                .thenReturn(true);
    }

    /** 이미 처리된 전송 ID의 결제 처리를 생략하는지 검증합니다. */
    @Test
    @DisplayName("이미 PROCESSED된 transmissionId면 handleWebhook을 호출하지 않고 200을 반환한다")
    void webhookSkipsAlreadyProcessedTransmission() {
        // given
        when(webhookEventService.isAlreadyProcessed("transmission-1"))
                .thenReturn(true);

        // when
        var response = paymentWebhookController.webhook(
                "Basic valid",
                "transmission-1",
                1,
                PAYLOAD,
                servletRequest
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, never()).handleWebhook(any());
        verify(webhookEventService, never()).recordReceived(
                any(), any(), any(), any(), any(Integer.class)
        );
    }

    /** 신규 전송 ID를 수신 기록한 뒤 처리 완료로 변경하는지 검증합니다. */
    @Test
    @DisplayName("신규 transmissionId면 수신 기록 후 처리하고 PROCESSED로 변경한다")
    void webhookRecordsAndProcessesNewTransmission() {
        // given
        when(webhookEventService.recordReceived(
                "transmission-1",
                "PAYMENT_STATUS_CHANGED",
                "order-1",
                PAYLOAD,
                0
        )).thenReturn(10L);

        // when
        var response = paymentWebhookController.webhook(
                "Basic valid",
                "transmission-1",
                null,
                PAYLOAD,
                servletRequest
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService).handleWebhook(any(PaymentWebhookRequest.class));
        verify(webhookEventService).markProcessed(10L);
    }

    /** 결제 처리 거부 시 실패를 기록하고 4xx 응답을 반환하는지 검증합니다. */
    @Test
    @DisplayName("처리 실패 시 FAILED로 기록하고 4xx를 반환한다")
    void webhookRecordsFailureAndReturnsClientError() {
        // given
        when(webhookEventService.recordReceived(
                "transmission-1",
                "PAYMENT_STATUS_CHANGED",
                "order-1",
                PAYLOAD,
                2
        )).thenReturn(10L);
        org.mockito.Mockito.doThrow(new CustomException(ErrorCode.INVALID_REQUEST))
                .when(paymentService)
                .handleWebhook(any(PaymentWebhookRequest.class));

        // when
        var response = paymentWebhookController.webhook(
                "Basic valid",
                "transmission-1",
                2,
                PAYLOAD,
                servletRequest
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(webhookEventService).markFailed(10L, ErrorCode.INVALID_REQUEST.getMessage(), 2);
    }

    /** 전송 ID 헤더가 없으면 이벤트 기록 없이 기존 결제 처리를 수행하는지 검증합니다. */
    @Test
    @DisplayName("transmissionId 헤더가 없으면 멱등성 처리 없이 기존 로직대로 동작한다")
    void webhookProcessesWithoutIdempotencyWhenTransmissionIdIsMissing() {
        // given

        // when
        var response = paymentWebhookController.webhook(
                "Basic valid",
                null,
                null,
                PAYLOAD,
                servletRequest
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService).handleWebhook(any(PaymentWebhookRequest.class));
        verify(webhookEventService, never()).isAlreadyProcessed(any());
        verify(webhookEventService, never()).recordReceived(
                any(), any(), any(), any(), any(Integer.class)
        );
    }
}