package com.momatic.domain.meeting.dto;

import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.entity.MeetingStatus;

/** 업로드 완료 응답 DTO입니다. */
public record MeetingUploadResponse(Long meetingId, String title, MeetingStatus status) {

    /**
     * Meeting 엔티티를 응답 DTO로 변환합니다.
     *
     * @param meeting 회의 엔티티
     * @return 업로드 응답 DTO
     */
    public static MeetingUploadResponse from(Meeting meeting) {
        return new MeetingUploadResponse(meeting.getId(), meeting.getTitle(), meeting.getStatus());
    }
}
