package com.momatic.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.payment.dto.PaymentWebhookRequest;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.global.api.ApiResponse;
import com.momatic.infra.toss.TossPaymentClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 토스페이먼츠 Webhook을 검증하고 처리합니다.
     * 처리 실패도 200 응답으로 반환하여 불필요한 재시도를 방지합니다.
     *
     * @param authorizationHeader Authorization 헤더
     * @param payload Webhook JSON 요청 본문
     * @param servletRequest HTTP 요청
     * @return Webhook 수신 결과
     */
    @PostMapping("/payments/webhook")
    public ApiResponse<Void> webhook(@RequestHeader(
                                             value = "Authorization",
                                             required = false
                                     ) String authorizationHeader,
                                     @RequestBody(required = false) String payload,
                                     HttpServletRequest servletRequest) {
        try {
            if (!tossPaymentClient.isValidWebhookAuthorization(authorizationHeader)) {
                log.warn("토스페이먼츠 Webhook 인증 실패: remoteAddress={}", servletRequest.getRemoteAddr());
                return ApiResponse.ok(null);
            }
            PaymentWebhookRequest request = objectMapper.readValue(payload, PaymentWebhookRequest.class);
            paymentService.handleWebhook(request);
        } catch (Exception exception) {
            log.error("토스페이먼츠 Webhook 처리 실패", exception);
        }
        return ApiResponse.ok(null);
    }
}
