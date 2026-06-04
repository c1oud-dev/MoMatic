package com.momatic.domain.team.dto;

import com.momatic.domain.team.entity.TeamMember;

/** 팀 구성원 응답 DTO입니다. */
public record TeamMemberResponse(Long id,
                                 Long userId,
                                 String email,
                                 String name,
                                 String role) {

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param member 팀 구성원 엔티티
     * @return 팀 구성원 응답
     */
    public static TeamMemberResponse from(TeamMember member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getName(),
                member.getRole().name()
        );
    }
}
