package com.momatic.domain.team.dto;

import com.momatic.domain.team.entity.TeamInvite;

import java.time.LocalDateTime;

/** 팀 초대 응답 DTO입니다. */
public record TeamInviteResponse(Long id,
                                 Long teamId,
                                 String inviteeEmail,
                                 String code,
                                 LocalDateTime expiredAt) {

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param invite 팀 초대 엔티티
     * @return 팀 초대 응답
     */
    public static TeamInviteResponse from(TeamInvite invite) {
        return new TeamInviteResponse(
                invite.getId(),
                invite.getTeam().getId(),
                invite.getInviteeEmail(),
                invite.getCode(),
                invite.getExpiredAt()
        );
    }
}
