package com.momatic.domain.team.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 팀 소속 구성원 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "team_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role;

    /**
     * 팀 구성원 엔티티를 생성합니다.
     *
     * @param team 소속 팀
     * @param user 구성원 사용자
     * @param role 팀 권한
     * @return 팀 구성원
     */
    public static TeamMember create(Team team,
                                    User user,
                                    TeamRole role) {
        TeamMember member = new TeamMember();
        member.team = team;
        member.user = user;
        member.role = role;
        return member;
    }

    /**
     * 팀 구성원의 권한을 변경합니다.
     *
     * @param role 변경할 팀 권한
     */
    public void updateRole(TeamRole role) {
        this.role = role;
    }

    /**
     * 팀 소유자 여부를 확인합니다.
     *
     * @return 팀 소유자 여부
     */
    public boolean isOwner() {
        return role == TeamRole.OWNER;
    }

    /**
     * 팀 관리 권한 여부를 확인합니다.
     *
     * @return 팀 관리 권한 여부
     */
    public boolean canManageTeam() {
        return role.canManageTeam();
    }
}
