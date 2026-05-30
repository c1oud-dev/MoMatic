package com.momatic.domain.meeting.controller;

import com.momatic.domain.meeting.dto.MeetingDetailResponse;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.dto.MeetingStatusResponse;
import com.momatic.domain.meeting.dto.MeetingUploadResponse;
import com.momatic.domain.meeting.service.MeetingUploadService;
import com.momatic.domain.meeting.service.MeetingService;

import com.momatic.global.api.ApiResponse;
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
                meetingService.getOwnedMeetingDetail(meetingId, getEmail(principal))
        );
        model.addAttribute("detail", meeting);
        return "meeting/detail";
    }

    /**
     * 회의 파일 업로드 페이지를 표시합니다.
     *
     * @return 업로드 템플릿 경로
     */
    @GetMapping("/upload")
    public String uploadPage() {
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
                meetingService.findOwnedMeeting(meetingId, getEmail(principal))
        ));
    }

    /**
     * 음성 파일 업로드를 수행합니다.
     *
     * @param userId 사용자 ID
     * @param teamId 팀 ID
     * @param title 회의 제목
     * @param planType 플랜 타입
     * @param file 음성 파일
     * @return 업로드 결과
     */
    @PostMapping("/upload")
    @ResponseBody
    public ApiResponse<MeetingUploadResponse> uploadMeetingFile(@RequestParam Long userId,
                                                                @RequestParam Long teamId,
                                                                @RequestParam String title,
                                                                @RequestParam String planType,
                                                                @RequestParam MultipartFile file) {
        return ApiResponse.ok(MeetingUploadResponse.from(
                meetingUploadService.upload(userId, teamId, title, planType, file)
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
