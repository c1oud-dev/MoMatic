package com.momatic.domain.payment.controller;

import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.domain.payment.dto.PaymentResponse;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.global.security.AuthenticatedUserResolver;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 결제 화면 요청을 처리하는 컨트롤러입니다. */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentPageController {

    private final PaymentService paymentService;

    @Value("${app.external.toss.payments.client-key}")
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
                AuthenticatedUserResolver.getEmail(principal),
                planType
        );
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("payment", PaymentResponse.from(payment));
        return "payment/checkout";
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
        log.info("결제 성공 콜백: orderId={}, paymentKey={}, amount={}", orderId, paymentKey, amount);
        paymentService.confirm(
                AuthenticatedUserResolver.getEmail(principal),
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
     * @param pageable 페이징 정보
     * @param model 화면 모델
     * @return 결제 내역 템플릿
     */
    @GetMapping(value = "/payments", produces = MediaType.TEXT_HTML_VALUE)
    public String payments(@AuthenticationPrincipal OAuth2User principal,
                           @PageableDefault(size = 10) Pageable pageable,
                           Model model) {
        Page<PaymentResponse> payments = paymentService.getPayments(
                AuthenticatedUserResolver.getEmail(principal),
                pageable
        ).map(PaymentResponse::from);
        model.addAttribute("payments", payments);
        return "payment/history";
    }
}