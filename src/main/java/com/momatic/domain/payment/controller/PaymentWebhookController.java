package com.momatic.domain.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.payment.dto.PaymentWebhookRequest;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.domain.payment.service.WebhookEventService;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.toss.TossPaymentClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** 결제 Webhook 요청을 처리하는 컨트롤러입니다. */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final WebhookEventService webhookEventService;

    /**
     * 토스페이먼츠 Webhook을 검증하고 처리합니다.
     * 요청 오류는 4xx로 반환하고 일시적인 서버 오류는 5xx로 반환하여 재전송을 유도합니다.
     *
     * @param authorizationHeader Authorization 헤더
     * @param transmissionId Webhook 고유 전송 ID
     * @param retriedCount 토스페이먼츠 재전송 횟수
     * @param payload Webhook JSON 요청 본문
     * @param servletRequest HTTP 요청
     * @return Webhook 수신 결과
     */
    @PostMapping("/payments/webhook")
    public ResponseEntity<ApiResponse<Void>> webhook(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @RequestHeader(
                    value = "tosspayments-webhook-transmission-id",
                    required = false
            ) String transmissionId,
            @RequestHeader(
                    value = "tosspayments-webhook-transmission-retried-count",
                    required = false
            ) Integer retriedCount,
            @RequestBody(required = false) String payload,
            HttpServletRequest servletRequest) {
        Long webhookEventId = null;
        int normalizedRetriedCount = retriedCount == null ? 0 : retriedCount;
        try {
            if (!tossPaymentClient.isValidWebhookAuthorization(authorizationHeader)) {
                log.warn("토스페이먼츠 Webhook 인증 실패: remoteAddress={}, payload={}",
                        servletRequest.getRemoteAddr(),
                        summarizePayload(payload));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail(
                                ErrorCode.UNAUTHORIZED.name(),
                                ErrorCode.UNAUTHORIZED.getMessage()
                        ));
            }
            if (payload == null) {
                log.error("토스페이먼츠 Webhook 본문이 없습니다: remoteAddress={}",
                        servletRequest.getRemoteAddr());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(
                                ErrorCode.INVALID_REQUEST.name(),
                                ErrorCode.INVALID_REQUEST.getMessage()
                        ));
            }
            boolean hasTransmissionId = transmissionId != null
                    && !transmissionId.isBlank();
            if (!hasTransmissionId) {
                log.warn(
                        "Webhook 전송 ID가 없어 멱등성 처리 없이 진행합니다: remoteAddress={}",
                        servletRequest.getRemoteAddr()
                );
            } else if (webhookEventService.isAlreadyProcessed(transmissionId)) {
                log.info("이미 처리된 Webhook 이벤트입니다: transmissionId={}", transmissionId);
                return ResponseEntity.ok(ApiResponse.ok(null));
            }
            PaymentWebhookRequest request = objectMapper.readValue(
                    payload,
                    PaymentWebhookRequest.class
            );
            if (hasTransmissionId) {
                webhookEventId = webhookEventService.recordReceived(
                        transmissionId,
                        request.eventType(),
                        extractOrderId(payload),
                        payload,
                        normalizedRetriedCount
                );
            }
            paymentService.handleWebhook(request);
            if (webhookEventId != null) {
                webhookEventService.markProcessed(webhookEventId);
            }
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (JsonProcessingException exception) {
            markFailed(webhookEventId, exception, normalizedRetriedCount);
            log.error("토스페이먼츠 Webhook JSON 파싱 실패: payload={}",
                    summarizePayload(payload),
                    exception);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(
                            ErrorCode.INVALID_REQUEST.name(),
                            ErrorCode.INVALID_REQUEST.getMessage()
                    ));
        } catch (CustomException exception) {
            markFailed(webhookEventId, exception, normalizedRetriedCount);
            ErrorCode errorCode = exception.getErrorCode();
            log.error("토스페이먼츠 Webhook 처리 거부: orderId={}, errorCode={}",
                    extractOrderId(payload),
                    errorCode.name(),
                    exception);
            return ResponseEntity.status(errorCode.getStatus())
                    .body(ApiResponse.fail(errorCode.name(), errorCode.getMessage()));
        } catch (Exception exception) {
            markFailed(webhookEventId, exception, normalizedRetriedCount);
            log.error("토스페이먼츠 Webhook 처리 실패: payload={}",
                    summarizePayload(payload),
                    exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(
                            ErrorCode.INTERNAL_ERROR.name(),
                            ErrorCode.INTERNAL_ERROR.getMessage()
                    ));
        }
    }

    /**
     * 수신 기록이 생성된 Webhook 이벤트의 처리 실패를 기록합니다.
     *
     * @param webhookEventId Webhook 이벤트 기록 ID
     * @param exception 처리 중 발생한 예외
     * @param retriedCount 토스페이먼츠 재전송 횟수
     */
    private void markFailed(Long webhookEventId,
                            Exception exception,
                            int retriedCount) {
        if (webhookEventId != null) {
            webhookEventService.markFailed(
                    webhookEventId,
                    exception.getMessage(),
                    retriedCount
            );
        }
    }

    /**
     * 로그에 기록할 수 있도록 요청 본문을 제한된 길이로 요약합니다.
     *
     * @param payload Webhook JSON 요청 본문
     * @return 제한된 길이의 요청 본문
     */
    private String summarizePayload(String payload) {
        if (payload == null) {
            return "null";
        }
        int maximumLength = 200;
        return payload.substring(0, Math.min(payload.length(), maximumLength));
    }

    /**
     * 오류 추적 로그에 사용할 주문 ID를 요청 본문에서 추출합니다.
     *
     * @param payload Webhook JSON 요청 본문
     * @return 주문 ID 또는 추출 실패 표시
     */
    private String extractOrderId(String payload) {
        try {
            return objectMapper.readTree(payload)
                    .path("data")
                    .path("orderId")
                    .asText("unknown");
        } catch (Exception exception) {
            return "unknown";
        }
    }
}
