package com.momatic.domain.meeting.controller;

import com.momatic.domain.meeting.dto.MeetingDetailResponse;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.service.TeamService;
import com.momatic.global.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 회의 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingPageController {

    private final MeetingService meetingService;
    private final TeamService teamService;

    /**
     * 인증 사용자가 소유한 회의 목록 페이지를 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @param model 화면 모델
     * @return 회의 목록 템플릿 경로
     */
    @GetMapping
    public String listMeetings(@AuthenticationPrincipal OAuth2User principal,
                               @RequestParam(required = false) String keyword,
                               @PageableDefault(size = 10) Pageable pageable,
                               Model model) {
        String searchKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        Page<MeetingResponse> meetings = meetingService.searchOwnedMeetings(
                AuthenticatedUserResolver.getEmail(principal),
                searchKeyword,
                pageable
        ).map(MeetingResponse::from);
        model.addAttribute("meetings", meetings);
        model.addAttribute("keyword", searchKeyword);
        return "meeting/list";
    }

    /**
     * 인증 사용자가 소유한 회의 상세 페이지를 표시합니다.
     *
     * @param meetingId 회의 ID
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 회의 상세 템플릿 경로
     */
    @GetMapping("/{meetingId}")
    public String getMeeting(@PathVariable Long meetingId,
                             @AuthenticationPrincipal OAuth2User principal,
                             Model model) {
        MeetingDetailResponse meeting = MeetingDetailResponse.from(
                meetingService.getAccessibleMeetingDetail(
                        meetingId,
                        AuthenticatedUserResolver.getEmail(principal)
                )
        );
        model.addAttribute("detail", meeting);
        return "meeting/detail";
    }

    /**
     * 회의 파일 업로드 페이지를 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 업로드 템플릿 경로
     */
    @GetMapping("/upload")
    public String uploadPage(@AuthenticationPrincipal OAuth2User principal,
                             Model model) {
        model.addAttribute("teams", teamService.findTeamsByMemberEmail(
                AuthenticatedUserResolver.getEmail(principal)
        ).stream().map(TeamResponse::from).toList());
        return "meeting/upload";
    }
}