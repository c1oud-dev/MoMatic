package com.momatic.domain.actionItem.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 액션 아이템 생성 및 수정 요청 DTO입니다.
 *
 * @param task 액션 아이템 내용
 * @param assignee 담당자
 * @param dueDate 마감일
 */
public record ActionItemRequest(
        @NotBlank String task,
        String assignee,
        LocalDate dueDate
) {
}
