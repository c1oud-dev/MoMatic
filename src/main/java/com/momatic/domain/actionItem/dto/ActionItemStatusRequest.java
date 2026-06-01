package com.momatic.domain.actionItem.dto;

import com.momatic.domain.actionItem.entity.ActionStatus;
/**
 * 액션 아이템 상태 변경 요청 DTO입니다.
 *
 * @param status 변경할 상태
 */
public record ActionItemStatusRequest(
        ActionStatus status
) {
}
