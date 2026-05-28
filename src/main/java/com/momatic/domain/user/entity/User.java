package com.momatic.domain.user.entity;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    /** 사용자 엔티티를 생성합니다. */
    public static User create(String email, String name, String role,
                              String provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.role = role;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    public void updateProfile(String name, String role) {
        this.name = name;
        this.role = role;
    }
}
