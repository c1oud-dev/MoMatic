package com.momatic.domain.meeting.dto;

import com.momatic.domain.meeting.entity.Meeting;

import java.time.LocalDateTime;

/**
 * 회의 목록 응답 DTO입니다.
 *
 * @param id 회의 ID
 * @param title 제목
 * @param startedAt 시작 시각
 * @param endedAt 종료 시각
 */
public record MeetingResponse(
        Long id,
        String title,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {

    /**
     * 회의 엔티티를 DTO로 변환합니다.
     *
     * @param meeting 회의 엔티티
     * @return 회의 응답 DTO
     */
    public static MeetingResponse from(final Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getStartedAt(),
                meeting.getEndedAt()
        );
    }
}
