package com.momatic.domain.dashboard.controller;

import com.momatic.domain.dashboard.dto.DashboardResponse;
import com.momatic.domain.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 대시보드 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 인증 사용자의 대시보드 페이지를 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 대시보드 템플릿 경로
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal,
                            Model model) {
        DashboardResponse dashboard = dashboardService.getDashboard(principal.getAttribute("email"));
        model.addAttribute("dashboard", dashboard);
        return "dashboard/index";
    }
}
