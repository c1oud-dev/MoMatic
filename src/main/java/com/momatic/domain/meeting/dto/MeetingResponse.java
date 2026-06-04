package com.momatic.domain.meeting.dto;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;

import java.time.LocalDateTime;

/**
 * 회의 화면 응답 DTO입니다.
 *
 * @param id 회의 ID
 * @param title 제목
 * @param status 처리 상태
 * @param startedAt 시작 시각
 * @param endedAt 종료 시각
 * @param summary 요약
 * @param teamId 팀 ID
 * @param teamName 팀 이름
 * @param createdAt 생성 시각
 */
public record MeetingResponse(
        Long id,
        String title,
        MeetingStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String summary,
        Long teamId,
        String teamName,
        LocalDateTime createdAt
) {

    /**
     * 회의 엔티티를 DTO로 변환합니다.
     *
     * @param meeting 회의 엔티티
     * @return 회의 응답 DTO
     */
    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getStatus(),
                meeting.getStartedAt(),
                meeting.getEndedAt(),
                meeting.getSummary(),
                meeting.hasTeam() ? meeting.getTeam().getId() : null,
                meeting.hasTeam() ? meeting.getTeam().getName() : null,
                meeting.getCreatedAt()
        );
    }
}
