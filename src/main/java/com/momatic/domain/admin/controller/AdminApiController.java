package com.momatic.domain.admin.controller;

import com.momatic.domain.admin.dto.AdminFileDeletionPageResponse;
import com.momatic.domain.admin.dto.AdminPlanChangeRequest;
import com.momatic.domain.admin.service.AdminUserService;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import com.momatic.domain.meeting.service.MeetingFileDeletionRetryService;
import com.momatic.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/** 관리자 콘솔 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminUserService adminUserService;
    private final MeetingFileDeletionRetryService meetingFileDeletionRetryService;

    /**
     * 파일 삭제 재시도 큐를 상태와 페이지 조건으로 조회합니다.
     *
     * @param status 조회할 상태, 전체 조회 시 {@code null}
     * @param pageable 페이지 요청 정보
     * @return 파일 삭제 재시도 목록과 상태 요약
     */
    @GetMapping(value = "/file-deletions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AdminFileDeletionPageResponse> fileDeletions(
            @RequestParam(required = false) FailedFileDeletionStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        AdminFileDeletionPageResponse response = AdminFileDeletionPageResponse.from(
                meetingFileDeletionRetryService.findAll(status, pageable),
                meetingFileDeletionRetryService.countByStatus(FailedFileDeletionStatus.PENDING),
                meetingFileDeletionRetryService.countByStatus(FailedFileDeletionStatus.GIVEN_UP)
        );
        return ApiResponse.ok(response);
    }

    /**
     * 포기된 파일 삭제 항목을 재시도 대기 상태로 되돌립니다.
     *
     * @param id 파일 삭제 기록 ID
     * @return API 공통 성공 응답
     */
    @PostMapping("/file-deletions/{id}/retry")
    public ApiResponse<Void> retryFileDeletion(@PathVariable Long id) {
        meetingFileDeletionRetryService.resetForManualRetry(id);
        return ApiResponse.ok(null);
    }

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
