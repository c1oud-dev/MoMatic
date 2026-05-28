package com.momatic.domain.meeting.controller;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.dto.MeetingDetailResponse;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.meeting.service.MeetingService;
import java.util.List;

import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회의 조회 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

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
}
