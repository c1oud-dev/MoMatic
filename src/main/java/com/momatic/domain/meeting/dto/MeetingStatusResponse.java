package com.momatic.domain.meeting.dto;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;

import java.time.LocalDateTime;

/**
 * 회의 처리 상태 응답 DTO입니다.
 *
 * @param meetingId 회의 ID
 * @param status 회의 처리 상태
 * @param updatedAt 마지막 수정 시각
 */
public record MeetingStatusResponse(
        Long meetingId,
        MeetingStatus status,
        LocalDateTime updatedAt
) {

    /**
     * 회의 엔티티를 상태 응답 DTO로 변환합니다.
     *
     * @param meeting 회의 엔티티
     * @return 회의 처리 상태 응답 DTO
     */
    public static MeetingStatusResponse from(Meeting meeting) {
        return new MeetingStatusResponse(
                meeting.getId(),
                meeting.getStatus(),
                meeting.getUpdatedAt()
        );
    }
}
