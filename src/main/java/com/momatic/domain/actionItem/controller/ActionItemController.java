package com.momatic.domain.actionItem.controller;

import com.momatic.domain.actionItem.dto.ActionItemStatusRequest;
import com.momatic.domain.actionItem.dto.ActionItemStatusResponse;
import com.momatic.domain.actionItem.service.ActionItemService;
import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 액션 아이템 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequestMapping("/action-items")
@RequiredArgsConstructor
public class ActionItemController {

    private final ActionItemService actionItemService;

    /**
     * 인증 사용자가 소유한 회의의 액션 아이템 상태를 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param request 상태 변경 요청
     * @param principal 인증 사용자 정보
     * @return 변경된 액션 아이템 상태 응답
     */
    @PatchMapping("/{actionItemId}/status")
    public ApiResponse<ActionItemStatusResponse> updateStatus(@PathVariable Long actionItemId,
                                                              @RequestBody ActionItemStatusRequest request,
                                                              @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(ActionItemStatusResponse.from(
                actionItemService.updateOwnedActionItemStatus(
                        actionItemId,
                        principal.getAttribute("email"),
                        request.status()
                )
        ));
    }
}

