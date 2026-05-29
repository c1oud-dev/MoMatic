package com.momatic.domain.meeting.controller;

import com.momatic.domain.meeting.dto.MeetingDetailResponse;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.dto.MeetingStatusResponse;
import com.momatic.domain.meeting.dto.MeetingUploadResponse;
import com.momatic.domain.meeting.service.MeetingUploadService;
import com.momatic.domain.meeting.service.MeetingService;
import java.util.List;

import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 회의 API 컨트롤러입니다. */
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingUploadService meetingUploadService;

    /**
     * 전체 회의 목록을 조회합니다.
     *
     * @return 회의 목록 응답
     */
    @GetMapping
    public ApiResponse<List<MeetingResponse>> listMeetings() {
        List<MeetingResponse> meetings = meetingService.findAllMeetings().stream()
                .map(MeetingResponse::from)
                .toList();
        return ApiResponse.ok(meetings);
    }

    /**
     * 단일 회의 상세 정보를 조회합니다.
     *
     * @param id 회의 ID
     * @return 회의 상세 응답
     */
    @GetMapping("/{id}")
    public ApiResponse<MeetingDetailResponse> getMeeting(@PathVariable Long id) {
        return ApiResponse.ok(MeetingDetailResponse.from(meetingService.getMeetingDetail(id)));
    }

    /**
     * 인증 사용자가 소유한 회의의 처리 상태를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param principal 인증 사용자 정보
     * @return 회의 처리 상태 응답
     */
    @GetMapping("/{meetingId}/status")
    public ApiResponse<MeetingStatusResponse> getMeetingStatus(@PathVariable Long meetingId,
                                                               @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return ApiResponse.ok(MeetingStatusResponse.from(meetingService.findOwnedMeeting(meetingId, email)));
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
    public ApiResponse<MeetingUploadResponse> uploadMeetingFile(@RequestParam Long userId,
                                                                @RequestParam Long teamId,
                                                                @RequestParam String title,
                                                                @RequestParam String planType,
                                                                @RequestParam MultipartFile file) {
        return ApiResponse.ok(MeetingUploadResponse.from(
                meetingUploadService.upload(userId, teamId, title, planType, file)
        ));
    }
}
