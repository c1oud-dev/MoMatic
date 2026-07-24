package com.momatic.domain.team.dto;

import jakarta.validation.constraints.NotBlank;

/** 팀 초대 요청 DTO입니다. */
public record TeamInviteRequest(@NotBlank String email) {
}
