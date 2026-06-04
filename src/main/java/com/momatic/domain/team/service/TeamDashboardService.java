package com.momatic.domain.team.service;

import com.momatic.domain.actionItem.entity.ActionStatus;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.team.dto.TeamDashboardResponse;
import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.entity.TeamMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 팀 대시보드 통계 조회를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class TeamDashboardService {

    private final TeamService teamService;
    private final MeetingRepository meetingRepository;
    private final ActionItemRepository actionItemRepository;

    /**
     * 팀 대시보드 통계를 조회합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @return 팀 대시보드 응답
     */
    @Transactional(readOnly = true)
    public TeamDashboardResponse getDashboard(Long teamId,
                                              String requesterEmail) {
        Team team = teamService.findTeamForMember(teamId, requesterEmail);
        List<TeamMember> members = teamService.findMembers(teamId, requesterEmail);
        long totalActionItemCount = actionItemRepository.countByMeetingTeamId(teamId);
        long incompleteActionItemCount = actionItemRepository.countByMeetingTeamIdAndStatusIn(
                teamId,
                List.of(ActionStatus.TODO, ActionStatus.IN_PROGRESS)
        );

        return new TeamDashboardResponse(
                TeamResponse.from(team),
                meetingRepository.countByTeamId(teamId),
                totalActionItemCount,
                incompleteActionItemCount,
                Math.max(totalActionItemCount - incompleteActionItemCount, 0),
                members.stream()
                        .map(member -> new TeamDashboardResponse.MemberParticipation(
                                member.getId(),
                                member.getUser().getId(),
                                member.getUser().getName(),
                                member.getUser().getEmail(),
                                member.getRole().name(),
                                meetingRepository.countByTeamIdAndOwnerId(teamId, member.getUser().getId())
                        ))
                        .toList()
        );
    }
}