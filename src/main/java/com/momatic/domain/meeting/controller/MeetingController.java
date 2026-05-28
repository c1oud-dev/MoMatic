package com.momatic.domain.meeting.controller;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.dto.MeetingDetailResponse;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.dto.MeetingUploadResponse;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.service.MeetingUploadService;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.meeting.service.MeetingService;
import java.util.List;

import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
