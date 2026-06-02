package com.momatic.domain.actionItem.dto;

import com.momatic.domain.actionItem.entity.ActionItem;

import java.time.LocalDate;

/**
 * 액션 아이템 응답 DTO입니다.
 *
 * @param id 액션 아이템 ID
 * @param task 액션 아이템 내용
 * @param assignee 담당자
 * @param dueDate 마감일
 * @param status 진행 상태
 */
public record ActionItemResponse(
        Long id,
        String task,
        String assignee,
        LocalDate dueDate,
        String status
) {

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param actionItem 액션 아이템 엔티티
     * @return 액션 아이템 응답 DTO
     */
    public static ActionItemResponse from(ActionItem actionItem) {
        return new ActionItemResponse(
                actionItem.getId(),
                actionItem.getTask(),
                actionItem.getAssignee(),
                actionItem.getDueDate(),
                actionItem.getStatus().name()
        );
    }
}