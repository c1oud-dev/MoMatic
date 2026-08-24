package com.momatic.domain.user.entity;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 서비스 사용자 정보를 표현하는 엔티티입니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private static final String ROLE_PREFIX = "ROLE_";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(length = 512)
    private String googleAccessToken;

    @Column(length = 512)
    private String googleRefreshToken;

    private LocalDateTime googleTokenExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    /**
     * 사용자 엔티티를 생성합니다.
     *
     * <p>Spring Security의 {@code hasRole}이 {@code ROLE_} 접두사가 포함된 권한을
     * 조회하므로 저장 시점에 권한 값을 동일한 형식으로 정규화합니다.</p>
     *
     * @param email 사용자 이메일
     * @param name 사용자 이름
     * @param role 사용자 권한
     * @param provider OAuth2 제공자
     * @param providerId OAuth2 제공자 사용자 ID
     * @return 생성된 사용자
     */
    public static User create(String email, String name, String role,
                              String provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.role = normalizeRole(role);
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    /**
     * 권한 값을 Spring Security 권한 형식으로 정규화합니다.
     *
     * @param role 정규화할 권한
     * @return {@code ROLE_} 접두사가 포함된 권한
     */
    private static String normalizeRole(String role) {
        return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    }

    /**
     * OAuth2 재로그인 시 변경 가능한 사용자 프로필 정보를 갱신합니다.
     *
     * @param name 사용자 이름
     */
    public void updateProfile(String name) {
        this.name = name;
    }

    /**
     * Google Calendar API 호출에 사용할 OAuth2 토큰 정보를 갱신합니다.
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresAt 액세스 토큰 만료 시각
     */
    public void updateGoogleToken(String accessToken,
                                  String refreshToken,
                                  LocalDateTime expiresAt) {
        this.googleAccessToken = accessToken;
        if (refreshToken != null && !refreshToken.isBlank()) {
            this.googleRefreshToken = refreshToken;
        }
        this.googleTokenExpiresAt = expiresAt;
    }
}
