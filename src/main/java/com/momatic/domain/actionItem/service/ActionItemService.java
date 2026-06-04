package com.momatic.domain.actionItem.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.service.MeetingService;
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
    private final MeetingService meetingService;

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템 상태를 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param requesterEmail 요청자 이메일
     * @param status 변경할 상태
     * @return 변경된 액션 아이템
     */
    @Transactional
    public ActionItem updateEditableActionItemStatus(Long actionItemId,
                                                     String requesterEmail,
                                                     ActionStatus status) {
        if (status == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        ActionItem actionItem = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
        meetingService.validateMeetingEditable(actionItem.getMeeting(), requesterEmail);
        actionItem.updateStatus(status);
        return actionItem;
    }
}

