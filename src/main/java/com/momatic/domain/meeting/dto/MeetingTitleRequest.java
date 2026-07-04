package com.momatic.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 회의 제목 변경 요청 DTO입니다.
 *
 * @param title 변경할 회의 제목
 */
public record MeetingTitleRequest(
        @NotBlank String title
) {
}
