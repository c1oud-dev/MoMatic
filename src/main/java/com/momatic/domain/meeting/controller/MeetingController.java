package com.momatic.domain.meeting.controller;

import com.momatic.domain.meeting.dto.*;
import com.momatic.domain.meeting.service.MeetingUploadService;
import com.momatic.domain.meeting.service.MeetingService;

import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.service.TeamService;
import com.momatic.global.api.ApiResponse;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 회의 화면 및 API 컨트롤러입니다. */
@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingUploadService meetingUploadService;
    private final TeamService teamService;

    /**
     * 인증 사용자가 소유한 회의 목록 페이지를 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param pageable 페이징 정보
     * @param model 화면 모델
     * @return 회의 목록 템플릿 경로
     */
    @GetMapping
    public String listMeetings(@AuthenticationPrincipal OAuth2User principal,
                               @PageableDefault(size = 10) Pageable pageable,
                               Model model) {
        Page<MeetingResponse> meetings = meetingService.findOwnedMeetings(getEmail(principal), pageable)
                .map(MeetingResponse::from);
        model.addAttribute("meetings", meetings);
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
                meetingService.getAccessibleMeetingDetail(meetingId, getEmail(principal))
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
        model.addAttribute("teams", teamService.findTeamsByMemberEmail(getEmail(principal)).stream()
                .map(TeamResponse::from)
                .toList());
        return "meeting/upload";
    }

    /**
     * 인증 사용자가 소유한 회의의 처리 상태를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param principal 인증 사용자 정보
     * @return 회의 처리 상태 응답
     */
    @GetMapping("/{meetingId}/status")
    @ResponseBody
    public ApiResponse<MeetingStatusResponse> getMeetingStatus(@PathVariable Long meetingId,
                                                               @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(MeetingStatusResponse.from(
                meetingService.findAccessibleMeeting(meetingId, getEmail(principal))
        ));
    }

    /**
     * 음성 파일 업로드를 수행합니다.
     *
     * @param userId 사용자 ID
     * @param teamId 팀 ID, 개인 회의록이면 null
     * @param title 회의 제목
     * @param file 음성 파일
     * @return 업로드 결과
     */
    @PostMapping("/upload")
    @ResponseBody
    public ApiResponse<MeetingUploadResponse> uploadMeetingFile(@RequestParam Long userId,
                                                                @RequestParam(required = false) @Nullable Long teamId,
                                                                @RequestParam String title,
                                                                @RequestParam MultipartFile file) {
        return ApiResponse.ok(MeetingUploadResponse.from(
                meetingUploadService.upload(userId, teamId, title, file)
        ));
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 제목을 변경합니다.
     *
     * @param meetingId 회의 ID
     * @param request 제목 변경 요청
     * @param principal 인증 사용자 정보
     * @return 성공 응답
     */
    @PatchMapping("/{meetingId}/title")
    @ResponseBody
    public ApiResponse<Void> updateMeetingTitle(@PathVariable Long meetingId,
                                                @Valid @RequestBody MeetingTitleRequest request,
                                                @AuthenticationPrincipal OAuth2User principal) {
        meetingService.updateTitle(meetingId, getEmail(principal), request.title());
        return ApiResponse.ok(null);
    }

    /**
     * 인증 사용자가 소유한 회의를 삭제합니다.
     *
     * @param meetingId 회의 ID
     * @param principal 인증 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{meetingId}")
    @ResponseBody
    public ApiResponse<Void> deleteMeeting(@PathVariable Long meetingId,
                                           @AuthenticationPrincipal OAuth2User principal) {
        meetingService.deleteMeeting(meetingId, getEmail(principal));
        return ApiResponse.ok(null);
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
