package com.momatic.domain.team.dto;

import com.momatic.domain.team.entity.Team;

/** 팀 응답 DTO입니다. */
public record TeamResponse(Long id, String name) {

    /** 엔티티를 DTO로 변환합니다. */
    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }
}
