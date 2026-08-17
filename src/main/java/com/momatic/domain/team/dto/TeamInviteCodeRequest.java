package com.momatic.domain.team.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 팀 초대 수락 또는 거절 요청을 표현하는 DTO입니다.
 *
 * @param code 팀 초대 코드
 */
public record TeamInviteCodeRequest(@NotBlank String code) {
}
