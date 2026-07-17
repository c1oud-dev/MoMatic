package com.momatic.domain.team.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 팀 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();

    /**
     * 팀을 생성하고 생성자를 소유자로 지정합니다.
     *
     * @param name 팀 이름
     * @param owner 팀 소유자
     * @return 생성된 팀
     */
    public static Team create(String name,
                              User owner) {
        Team team = new Team();
        team.name = name;
        team.addMember(owner, TeamRole.OWNER);
        return team;
    }

    /**
     * 팀 이름을 변경합니다.
     *
     * @param name 변경할 팀 이름
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 팀 구성원을 추가합니다.
     *
     * @param user 추가할 사용자
     * @param role 부여할 팀 권한
     * @return 추가된 팀 구성원
     */
    public TeamMember addMember(User user,
                                TeamRole role) {
        TeamMember member = TeamMember.create(this, user, role);
        members.add(member);
        return member;
    }
}
