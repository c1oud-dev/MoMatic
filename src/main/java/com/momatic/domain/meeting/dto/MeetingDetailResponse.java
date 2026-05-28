package com.momatic.domain.meeting.dto;

import com.momatic.domain.actionItem.dto.ActionItemResponse;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.domain.transcript.dto.TranscriptResponse;

import java.util.List;

/**
 * 회의 상세 응답 DTO입니다.
 *
 * @param meeting 회의 정보
 * @param actionItems 액션 아이템 목록
 * @param transcripts 전사 목록
 */
public record MeetingDetailResponse(
        MeetingResponse meeting,
        List<ActionItemResponse> actionItems,
        List<TranscriptResponse> transcripts
) {

    /**
     * 서비스 상세 조회 결과를 DTO로 변환합니다.
     *
     * @param detail 서비스 상세 조회 결과
     * @return 상세 응답 DTO
     */
    public static MeetingDetailResponse from(MeetingService.MeetingDetail detail) {
        return new MeetingDetailResponse(
                MeetingResponse.from(detail.meeting()),
                detail.actionItems().stream().map(ActionItemResponse::from).toList(),
                detail.transcripts().stream().map(TranscriptResponse::from).toList()
        );
    }
}

