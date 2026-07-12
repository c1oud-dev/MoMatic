package com.momatic.domain.subscription.controller;

import com.momatic.domain.payment.dto.PaymentResponse;
import com.momatic.domain.payment.service.PaymentService;
import com.momatic.domain.subscription.dto.SubscriptionSummary;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.usage.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 구독 현황 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final UsageRecordService usageRecordService;

    /**
     * 인증 사용자의 구독 현황 화면을 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param page 페이지 번호
     * @param model 화면 모델
     * @return 구독 현황 템플릿
     */
    @GetMapping("/subscription")
    public String subscription(@AuthenticationPrincipal OAuth2User principal,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        String email = principal.getAttribute("email");
        SubscriptionSummary subscription = subscriptionService.getSubscriptionSummary(email);
        Page<PaymentResponse> payments = paymentService.getPayments(
                email,
                PageRequest.of(page, 10, Sort.by("createdAt").descending())
        ).map(PaymentResponse::from);
        long monthlyUploadCount = usageRecordService.getMonthlyUploadCount(email);

        model.addAttribute("subscription", subscription);
        model.addAttribute("payments", payments);
        model.addAttribute("monthlyUploadCount", monthlyUploadCount);
        return "subscription/index";
    }
}