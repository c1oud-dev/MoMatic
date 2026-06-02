package com.momatic.domain.dashboard.dto;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.entity.Meeting;
import java.util.List;

/**
 * 대시보드 화면에 필요한 정보를 표현하는 DTO입니다.
 *
 * @param monthlyUploadCount 이번 달 업로드 횟수
 * @param remainingUploadCount 이번 달 남은 업로드 횟수
 * @param monthlyFileSizeBytes 이번 달 업로드 파일 용량
 * @param maxFileSizeBytes 플랜별 파일당 최대 용량
 * @param monthlyUploadLimit 플랜별 월 업로드 한도
 * @param planType 현재 플랜 타입
 * @param recentMeetings 최근 회의록 목록
 * @param incompleteActionItems 미완료 액션 아이템 목록
 */
public record DashboardResponse(
        long monthlyUploadCount,
        long monthlyUploadLimit,
        long remainingUploadCount,
        long monthlyFileSizeBytes,
        long maxFileSizeBytes,
        String planType,
        List<MeetingResponse> recentMeetings,
        List<ActionItemSummary> incompleteActionItems
) {

    /**
     * 대시보드에 표시할 액션 아이템 요약 DTO입니다.
     *
     * @param id 액션 아이템 ID
     * @param meetingId 회의 ID
     * @param meetingTitle 회의 제목
     * @param task 액션 아이템 내용
     * @param assignee 담당자
     * @param status 진행 상태
     */
    public record ActionItemSummary(
            Long id,
            Long meetingId,
            String meetingTitle,
            String task,
            String assignee,
            String status
    ) {

        /**
         * 액션 아이템 엔티티를 요약 DTO로 변환합니다.
         *
         * @param actionItem 액션 아이템 엔티티
         * @return 액션 아이템 요약 DTO
         */
        public static ActionItemSummary from(ActionItem actionItem) {
            return new ActionItemSummary(
                    actionItem.getId(),
                    actionItem.getMeeting().getId(),
                    actionItem.getMeeting().getTitle(),
                    actionItem.getTask(),
                    actionItem.getAssignee(),
                    actionItem.getStatus().name()
            );
        }
    }

    /**
     * 회의 엔티티 목록을 회의 화면 DTO 목록으로 변환합니다.
     *
     * @param meetings 회의 엔티티 목록
     * @return 회의 화면 DTO 목록
     */
    public static List<MeetingResponse> toMeetingResponses(List<Meeting> meetings) {
        return meetings.stream()
                .map(MeetingResponse::from)
                .toList();
    }
}

