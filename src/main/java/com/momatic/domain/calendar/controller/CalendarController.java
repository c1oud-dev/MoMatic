package com.momatic.domain.calendar.controller;

import com.momatic.domain.calendar.aop.CalendarPlanCheck;
import com.momatic.domain.calendar.service.GoogleCalendarService;
import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Google Calendar 연동 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    /**
     * 액션 아이템을 인증 사용자의 Google Calendar 일정으로 등록합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param principal 인증 사용자 정보
     * @return 성공 응답
     */
    @CalendarPlanCheck
    @PostMapping("/action-items/{actionItemId}/calendar")
    public ApiResponse<Void> createCalendarEvent(@PathVariable Long actionItemId,
                                                 @AuthenticationPrincipal OAuth2User principal) {
        googleCalendarService.createEvent(actionItemId, principal.getAttribute("email"));
        return ApiResponse.ok(null);
    }
}

