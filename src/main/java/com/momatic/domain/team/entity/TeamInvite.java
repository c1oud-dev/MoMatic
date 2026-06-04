package com.momatic.domain.team.entity;

import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** 팀 초대 정보를 표현하는 엔티티입니다. */
@Entity
@Table(name = "team_invites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamInvite extends BaseEntity {

    private static final long EXPIRATION_HOURS = 72L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(nullable = false)
    private String inviteeEmail;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean accepted;

    /**
     * 팀 초대를 생성합니다.
     *
     * @param team 초대 대상 팀
     * @param inviter 초대자
     * @param inviteeEmail 초대 대상 이메일
     * @return 생성된 팀 초대
     */
    public static TeamInvite create(Team team,
                                    User inviter,
                                    String inviteeEmail) {
        TeamInvite invite = new TeamInvite();
        invite.team = team;
        invite.inviter = inviter;
        invite.inviteeEmail = inviteeEmail;
        invite.code = UUID.randomUUID().toString();
        invite.expiredAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
        invite.accepted = false;
        return invite;
    }

    /** 초대를 수락 처리합니다. */
    public void accept() {
        this.accepted = true;
    }

    /**
     * 만료 여부를 확인합니다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
}

