package com.momatic.domain.user.dto;

import com.momatic.domain.user.entity.User;

/** 사용자 응답 DTO입니다. */
public record UserResponse(
        Long id,
        String email,
        String name,
        String role
) {

    /** 엔티티를 DTO로 변환합니다. */
    public static UserResponse from(final User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
