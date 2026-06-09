package com.momatic.domain.team.dto;

import java.util.List;

/**
 * 팀 대시보드 화면 응답 DTO입니다.
 *
 * @param team 팀 정보
 * @param totalMeetingCount 팀 전체 회의 수
 * @param totalActionItemCount 팀 전체 액션 아이템 수
 * @param incompleteActionItemCount 미완료 액션 아이템 수
 * @param completedActionItemCount 완료 액션 아이템 수
 * @param memberParticipations 구성원별 참여 현황
 */
public record TeamDashboardResponse(
        TeamResponse team,
        long totalMeetingCount,
        long totalActionItemCount,
        long incompleteActionItemCount,
        long completedActionItemCount,
        List<MemberParticipation> memberParticipations
) {

    /**
     * 팀 구성원별 참여 현황 DTO입니다.
     *
     * @param memberId 팀 구성원 ID
     * @param userId 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param role 팀 권한
     * @param uploadedMeetingCount 업로드한 팀 회의 수
     */
    public record MemberParticipation(
            Long memberId,
            Long userId,
            String name,
            String email,
            String role,
            long uploadedMeetingCount
    ) {
    }
}
