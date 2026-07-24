package com.momatic.domain.team.dto;

import jakarta.validation.constraints.NotBlank;

/** 팀 구성원 권한 변경 요청 DTO입니다. */
public record TeamRoleUpdateRequest(@NotBlank String role) {
}