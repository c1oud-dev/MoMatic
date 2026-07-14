package com.momatic.domain.admin.dto;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 관리자 사용자 목록 화면에 표시할 사용자 응답 DTO입니다.
 *
 * @param id 사용자 ID
 * @param email 사용자 이메일
 * @param name 사용자 이름
 * @param role 사용자 권한
 * @param planType 현재 플랜 타입
 * @param createdAt 가입 일시
 */
public record AdminUserResponse(
        Long id,
        String email,
        String name,
        String role,
        String planType,
        LocalDateTime createdAt
) {

    /**
     * 사용자 엔티티와 활성 구독 정보로 관리자 사용자 응답 DTO를 생성합니다.
     *
     * @param user 사용자 엔티티
     * @param subscription 활성 구독 정보
     * @return 관리자 사용자 응답 DTO
     */
    public static AdminUserResponse from(User user,
                                         Optional<Subscription> subscription) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                subscription.map(value -> value.getPlanType().name()).orElse("FREE"),
                user.getCreatedAt()
        );
    }
}