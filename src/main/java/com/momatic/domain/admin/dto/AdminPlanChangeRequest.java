package com.momatic.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 관리자 플랜 수동 변경 요청 DTO입니다.
 *
 * @param planType 변경할 플랜 타입
 */
public record AdminPlanChangeRequest(
        @NotBlank String planType
) {
}