package com.momatic.domain.plan.controller;

import com.momatic.domain.plan.dto.PlanResponse;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.service.SubscriptionService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 요금제 화면 요청과 플랜 변경을 처리하는 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class PlanController {

    private final SubscriptionService subscriptionService;

    /**
     * 공개 요금제 비교 화면을 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 요금제 템플릿 경로
     */
    @GetMapping("/plans")
    public String plans(@AuthenticationPrincipal OAuth2User principal,
                        Model model) {
        List<PlanResponse> plans = Arrays.stream(PlanPolicy.values())
                .map(PlanResponse::from)
                .toList();
        model.addAttribute("plans", plans);
        model.addAttribute("authenticated", principal != null);

        if (principal != null) {
            String email = principal.getAttribute("email");
            String currentPlan = subscriptionService.getActiveSubscription(email)
                    .map(Subscription::getPlanType)
                    .orElse(PlanPolicy.FREE)
                    .name();
            model.addAttribute("currentPlan", currentPlan);
        }

        return "plan/index";
    }
}