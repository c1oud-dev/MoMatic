package com.momatic.domain.admin.controller;

import com.momatic.domain.admin.dto.AdminPlanChangeRequest;
import com.momatic.domain.admin.dto.AdminSubscriptionResponse;
import com.momatic.domain.admin.dto.AdminUserResponse;
import com.momatic.domain.admin.service.AdminUserService;
import com.momatic.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** 관리자 콘솔 화면과 API 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    /**
     * 사용자 목록 화면을 표시합니다.
     *
     * @param pageable 페이지 요청 정보
     * @param model 화면 모델
     * @return 사용자 목록 템플릿
     */
    @GetMapping("/users")
    public String users(@PageableDefault(size = 10) Pageable pageable,
                        Model model) {
        Page<AdminUserResponse> users = adminUserService.findAllUsers(pageable)
                .map(user -> AdminUserResponse.from(
                        user,
                        adminUserService.getActiveSubscription(user.getEmail())
                ));

        model.addAttribute("users", users);
        return "admin/users";
    }

    /**
     * 구독 현황 화면을 표시합니다.
     *
     * @param pageable 페이지 요청 정보
     * @param model 화면 모델
     * @return 구독 현황 템플릿
     */
    @GetMapping("/subscriptions")
    public String subscriptions(@PageableDefault(size = 10) Pageable pageable,
                                Model model) {
        Page<AdminSubscriptionResponse> subscriptions = adminUserService.findAllSubscriptions(pageable)
                .map(AdminSubscriptionResponse::from);

        model.addAttribute("subscriptions", subscriptions);
        return "admin/subscriptions";
    }

    /**
     * 관리자 권한으로 사용자 플랜을 수동 변경합니다.
     *
     * @param userId 사용자 ID
     * @param request 플랜 변경 요청
     * @return API 공통 성공 응답
     */
    @PostMapping("/users/{userId}/plan")
    @ResponseBody
    public ApiResponse<Void> changePlan(@PathVariable Long userId,
                                        @Valid @RequestBody AdminPlanChangeRequest request) {
        adminUserService.changePlan(userId, request.planType());
        return ApiResponse.ok(null);
    }
}