package com.momatic.domain.actionItem.dto;

import com.momatic.domain.actionItem.entity.ActionItem;

/** 액션 아이템 응답 DTO입니다. */
public record ActionItemResponse(
        Long id,
        String task,
        String assignee,
        String status
) {

    /** 엔티티를 DTO로 변환합니다. */
    public static ActionItemResponse from(final ActionItem actionItem) {
        return new ActionItemResponse(
                actionItem.getId(),
                actionItem.getTask(),
                actionItem.getAssignee(),
                actionItem.getStatus().name()
        );
    }
}