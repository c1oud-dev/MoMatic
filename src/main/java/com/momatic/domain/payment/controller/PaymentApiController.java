package com.momatic.domain.payment.controller;

import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.domain.payment.dto.PaymentResponse;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 결제 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    /**
     * AJAX 결제 승인 요청을 처리합니다.
     *
     * @param principal 인증 사용자 정보
     * @param request 결제 승인 요청
     * @return 승인 완료 결제 응답
     */
    @PostMapping("/payments/confirm")
    public ApiResponse<PaymentResponse> confirm(@AuthenticationPrincipal OAuth2User principal,
                                                @Valid @RequestBody PaymentConfirmRequest request) {
        return ApiResponse.ok(PaymentResponse.from(paymentService.confirm(
                AuthenticatedUserResolver.getEmail(principal),
                request
        )));
    }

    /**
     * 인증 사용자의 결제 내역을 JSON API 응답으로 조회합니다.
     *
     * @param principal 인증 사용자 정보
     * @param pageable 페이징 정보
     * @return 결제 내역 API 응답
     */
    @GetMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Page<PaymentResponse>> paymentApi(@AuthenticationPrincipal OAuth2User principal,
                                                         @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(paymentService.getPayments(
                AuthenticatedUserResolver.getEmail(principal),
                pageable
        ).map(PaymentResponse::from));
    }

    /**
     * 인증 사용자의 활성 유료 구독을 취소 요청 처리합니다.
     *
     * @param principal 인증 사용자 정보
     * @return 구독 취소 처리 결과
     */
    @PostMapping("/subscription/cancel")
    public ApiResponse<Void> cancelSubscription(@AuthenticationPrincipal OAuth2User principal) {
        subscriptionService.cancelSubscription(AuthenticatedUserResolver.getEmail(principal));
        return ApiResponse.ok(null);
    }
}
