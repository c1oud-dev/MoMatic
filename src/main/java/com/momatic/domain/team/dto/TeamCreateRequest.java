package com.momatic.domain.team.dto;

import jakarta.validation.constraints.NotBlank;

/** 팀 생성 요청 DTO입니다. */
public record TeamCreateRequest(@NotBlank String name) {
}