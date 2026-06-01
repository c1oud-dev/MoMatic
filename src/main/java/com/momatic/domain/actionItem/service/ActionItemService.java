package com.momatic.domain.actionItem.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 액션 아이템 변경을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;

    /**
     * 인증 사용자가 소유한 회의의 액션 아이템 상태를 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param ownerEmail 회의 소유자 이메일
     * @param status 변경할 상태
     * @return 변경된 액션 아이템
     */
    @Transactional
    public ActionItem updateOwnedActionItemStatus(Long actionItemId,
                                                  String ownerEmail,
                                                  ActionStatus status) {
        if (status == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        ActionItem actionItem = actionItemRepository.findByIdAndMeetingOwnerEmail(actionItemId, ownerEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
        actionItem.updateStatus(status);
        return actionItem;
    }
}

