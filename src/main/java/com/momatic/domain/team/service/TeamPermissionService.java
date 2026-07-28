package com.momatic.domain.team.service;

import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.repository.TeamMemberRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 팀 소속 및 관리 권한을 검증하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class TeamPermissionService {

    private final TeamMemberRepository teamMemberRepository;

    /**
     * 팀 소속 여부를 확인합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 요청자의 팀 구성원 정보
     */
    public TeamMember requireMembership(Long teamId,
                                        Long userId) {
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
    }

    /**
     * 팀 관리 권한을 확인합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 요청자의 팀 구성원 정보
     */
    public TeamMember requireManagePermission(Long teamId,
                                              Long userId) {
        TeamMember member = requireMembership(teamId, userId);
        if (!member.canManageTeam()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return member;
    }
}