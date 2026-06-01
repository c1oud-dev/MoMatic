package com.momatic.domain.actionItem.dto;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;

/**
 * 액션 아이템 상태 변경 응답 DTO입니다.
 *
 * @param actionItemId 액션 아이템 ID
 * @param status 변경된 상태
 */
public record ActionItemStatusResponse(
        Long actionItemId,
        ActionStatus status
) {

    /**
     * 액션 아이템 엔티티를 상태 변경 응답 DTO로 변환합니다.
     *
     * @param actionItem 액션 아이템 엔티티
     * @return 상태 변경 응답 DTO
     */
    public static ActionItemStatusResponse from(ActionItem actionItem) {
        return new ActionItemStatusResponse(
                actionItem.getId(),
                actionItem.getStatus()
        );
    }
}
