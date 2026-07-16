package com.momatic.domain.team.dto;

import jakarta.validation.constraints.NotBlank;

/** 팀 이름 변경 요청 DTO입니다. */
public record TeamNameUpdateRequest(@NotBlank String name) {
}