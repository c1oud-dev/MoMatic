package com.momatic.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.domain.payment.dto.PaymentResponse;
import com.momatic.domain.payment.dto.PaymentWebhookRequest;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.global.api.ApiResponse;
import com.momatic.infra.toss.TossPaymentClient;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** 결제창 호출, 승인, Webhook, 결제 내역 화면 요청을 처리하는 컨트롤러입니다. */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.client-key}")
    private String clientKey;

    /**
     * 결제창을 호출할 승인 대기 주문 화면을 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param planType 결제 플랜 타입
     * @param model 화면 모델
     * @return 결제창 호출 템플릿
     */
    @PostMapping("/payments/checkout")
    public String checkout(@AuthenticationPrincipal OAuth2User principal,
                           @RequestParam String planType,
                           Model model) {
        Payment payment = paymentService.createPendingPayment(
                principal.getAttribute("email"),
                planType
        );
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("payment", PaymentResponse.from(payment));
        return "payment/checkout";
    }

    /**
     * AJAX 결제 승인 요청을 처리합니다.
     *
     * @param principal 인증 사용자 정보
     * @param request 결제 승인 요청
     * @return 승인 완료 결제 응답
     */
    @ResponseBody
    @PostMapping("/payments/confirm")
    public ApiResponse<PaymentResponse> confirm(@AuthenticationPrincipal OAuth2User principal,
                                                @RequestBody PaymentConfirmRequest request) {
        return ApiResponse.ok(PaymentResponse.from(paymentService.confirm(
                principal.getAttribute("email"),
                request
        )));
    }

    /**
     * 토스페이먼츠 성공 리다이렉트를 승인 처리한 뒤 결제 내역으로 이동합니다.
     *
     * @param principal 인증 사용자 정보
     * @param paymentKey 토스페이먼츠 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 결제 내역 리다이렉트 경로
     */
    @GetMapping("/payments/success")
    public String success(@AuthenticationPrincipal OAuth2User principal,
                          @RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam BigDecimal amount) {
        paymentService.confirm(
                principal.getAttribute("email"),
                new PaymentConfirmRequest(orderId, paymentKey, amount)
        );
        return "redirect:/payments";
    }

    /**
     * 토스페이먼츠 실패 리다이렉트 화면을 표시합니다.
     *
     * @param code 실패 코드
     * @param message 실패 메시지
     * @param model 화면 모델
     * @return 결제 실패 템플릿
     */
    @GetMapping("/payments/fail")
    public String fail(@RequestParam(required = false) String code,
                       @RequestParam(required = false) String message,
                       Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "payment/fail";
    }

    /**
     * 인증 사용자의 결제 내역 화면을 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 결제 내역 템플릿
     */
    @GetMapping("/payments")
    public String payments(@AuthenticationPrincipal OAuth2User principal,
                           Model model) {
        List<PaymentResponse> payments = paymentService.getPayments(principal.getAttribute("email"))
                .stream()
                .map(PaymentResponse::from)
                .toList();
        model.addAttribute("payments", payments);
        return "payment/list";
    }

    /**
     * 토스페이먼츠 Webhook을 검증하고 처리합니다.
     * 처리 실패도 200 응답으로 반환하여 불필요한 재시도를 방지합니다.
     *
     * @param authorizationHeader Authorization 헤더
     * @param payload Webhook JSON 요청 본문
     * @param servletRequest HTTP 요청
     * @return Webhook 수신 결과
     */
    @ResponseBody
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
