package com.momatic.domain.meeting.controller;

import com.momatic.domain.meeting.aop.MeetingPdfPlanCheck;
import com.momatic.domain.meeting.dto.*;
import com.momatic.domain.meeting.service.MeetingPdfService;
import com.momatic.domain.meeting.service.MeetingUploadService;
import com.momatic.domain.meeting.service.MeetingService;

import com.momatic.global.api.ApiResponse;
import com.momatic.global.security.AuthenticatedUserResolver;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 회의 API 요청을 처리하는 컨트롤러입니다. */
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingApiController {

    private final MeetingService meetingService;
    private final MeetingPdfService meetingPdfService;
    private final MeetingUploadService meetingUploadService;

    /**
     * 접근 가능한 회의록 상세 데이터를 PDF 파일로 다운로드합니다.
     *
     * @param meetingId 회의 ID
     * @param principal 인증 사용자 정보
     * @return PDF 다운로드 응답
     */
    @GetMapping("/{meetingId}/pdf")
    @MeetingPdfPlanCheck
    public ResponseEntity<byte[]> downloadMeetingPdf(@PathVariable Long meetingId,
                                                     @AuthenticationPrincipal OAuth2User principal) {
        byte[] pdf = meetingPdfService.generatePdf(meetingId, AuthenticatedUserResolver.getEmail(principal));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"meeting-" + meetingId + ".pdf\""
                )
                .body(pdf);
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
        return ApiResponse.ok(MeetingStatusResponse.from(
                meetingService.findAccessibleMeeting(meetingId, AuthenticatedUserResolver.getEmail(principal))
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
    public ApiResponse<Void> updateMeetingTitle(@PathVariable Long meetingId,
                                                @Valid @RequestBody MeetingTitleRequest request,
                                                @AuthenticationPrincipal OAuth2User principal) {
        meetingService.updateTitle(meetingId, AuthenticatedUserResolver.getEmail(principal), request.title());
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
    public ApiResponse<Void> deleteMeeting(@PathVariable Long meetingId,
                                           @AuthenticationPrincipal OAuth2User principal) {
        meetingService.deleteMeeting(meetingId, AuthenticatedUserResolver.getEmail(principal));
        return ApiResponse.ok(null);
    }
}
