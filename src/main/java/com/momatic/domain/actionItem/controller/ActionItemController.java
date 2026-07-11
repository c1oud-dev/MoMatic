package com.momatic.domain.actionItem.controller;

import com.momatic.domain.actionItem.dto.ActionItemRequest;
import com.momatic.domain.actionItem.dto.ActionItemResponse;
import com.momatic.domain.actionItem.dto.ActionItemStatusRequest;
import com.momatic.domain.actionItem.dto.ActionItemStatusResponse;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.service.ActionItemService;
import com.momatic.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 액션 아이템 API 및 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class ActionItemController {

    private final ActionItemService actionItemService;

    /**
     * 인증 사용자가 소유한 회의한 전체 액션 아이템 목록 페이지를 표시합니다.
     * @param status 조회할 액션 아이템 상태, 전체 조회이면 null
     * @param sort 정렬할 필드명
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 액션 아이템 목록 템플릿 경로
     */
    @GetMapping("/action-items")
    public String listActionItems(@RequestParam(required = false) ActionStatus status,
                                  @RequestParam(defaultValue = "dueDate") String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @AuthenticationPrincipal OAuth2User principal,
                                  Model model) {
        Page<ActionItemResponse> actionItems = actionItemService.findAllByOwner(
                getEmail(principal),
                status,
                PageRequest.of(page, size, Sort.by(sort))
        ).map(ActionItemResponse::from);
        model.addAttribute("actionItems", actionItems);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ActionStatus.values());
        return "action-items/list";
    }

    /**
     * 인증 사용자가 편집 가능한 회의에 액션 아이템을 수동 추가합니다.
     *
     * @param meetingId 회의 ID
     * @param request 액션 아이템 생성 요청
     * @param principal 인증 사용자 정보
     * @return 생성된 액션 아이템 응답
     */
    @PostMapping("/meetings/{meetingId}/action-items")
    @ResponseBody
    public ApiResponse<ActionItemResponse> addActionItem(@PathVariable Long meetingId,
                                                         @Valid @RequestBody ActionItemRequest request,
                                                         @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(ActionItemResponse.from(
                actionItemService.addActionItem(
                        meetingId,
                        getEmail(principal),
                        request.task(),
                        request.assignee(),
                        request.dueDate()
                )
        ));
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템 내용을 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param request 액션 아이템 수정 요청
     * @param principal 인증 사용자 정보
     * @return 성공 응답
     */
    @PatchMapping("/action-items/{actionItemId}")
    @ResponseBody
    public ApiResponse<Void> updateActionItem(@PathVariable Long actionItemId,
                                              @Valid @RequestBody ActionItemRequest request,
                                              @AuthenticationPrincipal OAuth2User principal) {
        actionItemService.updateActionItem(
                actionItemId,
                getEmail(principal),
                request.task(),
                request.assignee(),
                request.dueDate()
        );
        return ApiResponse.ok(null);
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템을 삭제합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param principal 인증 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/action-items/{actionItemId}")
    @ResponseBody
    public ApiResponse<Void> deleteActionItem(@PathVariable Long actionItemId,
                                              @AuthenticationPrincipal OAuth2User principal) {
        actionItemService.deleteActionItem(actionItemId, getEmail(principal));
        return ApiResponse.ok(null);
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템 상태를 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param request 상태 변경 요청
     * @param principal 인증 사용자 정보
     * @return 변경된 액션 아이템 상태 응답
     */
    @PatchMapping("/action-items/{actionItemId}/status")
    @ResponseBody
    public ApiResponse<ActionItemStatusResponse> updateStatus(@PathVariable Long actionItemId,
                                                              @RequestBody ActionItemStatusRequest request,
                                                              @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(ActionItemStatusResponse.from(
                actionItemService.updateEditableActionItemStatus(
                        actionItemId,
                        getEmail(principal),
                        request.status()
                )
        ));
    }

    /**
     * 인증 사용자 정보에서 이메일을 조회합니다.
     *
     * @param principal 인증 사용자 정보
     * @return 인증 사용자 이메일
     */
    private String getEmail(OAuth2User principal) {
        return principal.getAttribute("email");
    }
}

