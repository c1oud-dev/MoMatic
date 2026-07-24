package com.momatic.domain.admin.controller;

import com.momatic.domain.admin.dto.AdminPlanChangeRequest;
import com.momatic.domain.admin.service.AdminUserService;
import com.momatic.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 콘솔 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminUserService adminUserService;

    /**
     * 관리자 권한으로 사용자 플랜을 수동 변경합니다.
     *
     * @param userId 사용자 ID
     * @param request 플랜 변경 요청
     * @return API 공통 성공 응답
     */
    @PostMapping("/users/{userId}/plan")
    public ApiResponse<Void> changePlan(@PathVariable Long userId,
                                        @Valid @RequestBody AdminPlanChangeRequest request) {
        adminUserService.changePlan(userId, request.planType());
        return ApiResponse.ok(null);
    }
}
