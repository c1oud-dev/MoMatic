package com.momatic.domain.actionItem.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** 액션 아이템 변경을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final MeetingService meetingService;

    /**
     * 인증 사용자가 편집 가능한 회의에 액션 아이템을 수동 추가합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     * @param task 액션 아이템 내용
     * @param assignee 담당자
     * @param dueDate 마감일
     * @return 생성된 액션 아이템
     */
    @Transactional
    public ActionItem addActionItem(Long meetingId,
                                    String requesterEmail,
                                    String task,
                                    String assignee,
                                    LocalDate dueDate) {
        Meeting meeting = meetingService.findMeeting(meetingId);
        meetingService.validateMeetingEditable(meeting, requesterEmail);
        ActionItem actionItem = ActionItem.create(task, assignee, dueDate);
        actionItem.assignMeeting(meeting);
        return actionItemRepository.save(actionItem);
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템 내용을 변경합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param requesterEmail 요청자 이메일
     * @param task 액션 아이템 내용
     * @param assignee 담당자
     * @param dueDate 마감일
     */
    @Transactional
    public void updateActionItem(Long actionItemId,
                                 String requesterEmail,
                                 String task,
                                 String assignee,
                                 LocalDate dueDate) {
        ActionItem actionItem = findEditableActionItem(actionItemId, requesterEmail);
        actionItem.update(task, assignee, dueDate);
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 액션 아이템을 삭제합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param requesterEmail 요청자 이메일
     */
    @Transactional
    public void deleteActionItem(Long actionItemId,
                                 String requesterEmail) {
        ActionItem actionItem = findEditableActionItem(actionItemId, requesterEmail);
        actionItemRepository.delete(actionItem);
    }

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
        ActionItem actionItem = findEditableActionItem(actionItemId, requesterEmail);
        actionItem.updateStatus(status);
        return actionItem;
    }

    /**
     * 액션 아이템을 조회하고 편집 가능 여부를 검증합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param requesterEmail 요청자 이메일
     * @return 편집 가능한 액션 아이템
     */
    private ActionItem findEditableActionItem(Long actionItemId,
                                              String requesterEmail) {
        ActionItem actionItem = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
        meetingService.validateMeetingEditable(actionItem.getMeeting(), requesterEmail);
        return actionItem;
    }
}