package com.momatic.domain.actionItem.controller;

import com.momatic.domain.actionItem.dto.ActionItemResponse;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.service.ActionItemService;
import com.momatic.global.security.AuthenticatedUserResolver;
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

/** 액션 아이템 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class ActionItemPageController {

    private final ActionItemService actionItemService;

    /**
     * 인증 사용자가 소유한 회의한 전체 액션 아이템 목록 페이지를 표시합니다.
     *
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
                AuthenticatedUserResolver.getEmail(principal),
                status,
                PageRequest.of(page, size, Sort.by(sort))
        ).map(ActionItemResponse::from);
        model.addAttribute("actionItems", actionItems);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ActionStatus.values());
        return "action-items/list";
    }
}
