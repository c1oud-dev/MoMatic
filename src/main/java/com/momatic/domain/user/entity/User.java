package com.momatic.domain.user.entity;

import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 서비스 사용자 정보를 표현하는 엔티티입니다.
 */
@Entity
@Table(name = "users")
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

    protected User() {}

    public static User create(final String email, final String name, final String role,
                              final String provider, final String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.role = role;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    public void updateProfile(final String name, final String role) {
        this.name = name;
        this.role = role;
    }
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
