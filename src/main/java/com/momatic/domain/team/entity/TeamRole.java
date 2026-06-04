package com.momatic.domain.team.entity;

import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;

import java.util.Arrays;

/** 팀 구성원의 권한을 정의하는 열거형입니다. */
public enum TeamRole {
    OWNER,
    ADMIN,
    MEMBER;

    /**
     * 문자열에 해당하는 팀 권한을 조회합니다.
     *
     * @param role 팀 권한 문자열
     * @return 팀 권한
     */
    public static TeamRole from(String role) {
        if (role == null) {
            throw new CustomException(ErrorCode.INVALID_TEAM_ROLE);
        }

        return Arrays.stream(values())
                .filter(teamRole -> teamRole.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TEAM_ROLE));
    }

    /**
     * 팀 관리 권한 여부를 확인합니다.
     *
     * @return 팀 관리 권한 여부
     */
    public boolean canManageTeam() {
        return this == OWNER || this == ADMIN;
    }
}
