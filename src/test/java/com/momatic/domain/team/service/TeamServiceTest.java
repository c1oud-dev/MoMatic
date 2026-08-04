package com.momatic.domain.team.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.entity.TeamInvite;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.repository.TeamInviteRepository;
import com.momatic.domain.team.repository.TeamMemberRepository;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.mail.TeamInviteMailService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    private static final Long TEAM_ID = 10L;
    private static final String INVITER_EMAIL = "inviter@example.com";
    private static final String INVITEE_EMAIL = "invitee@example.com";

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamInviteRepository teamInviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamPermissionService teamPermissionService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private TeamInviteMailService teamInviteMailService;

    @InjectMocks
    private TeamService teamService;

    @Test
    @DisplayName("TEAM 플랜이 아닌 사용자가 팀을 생성하면 예외가 발생한다")
    void createTeamThrowsWhenUserDoesNotHaveTeamPlan() {
        // given
        User owner = createUser(1L, "owner@example.com", "팀 소유자");
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(subscriptionService.getActivePlan(owner.getId()))
                .thenReturn(PlanPolicy.FREE);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.createTeam(owner.getEmail(), "팀 이름")
        );

        // then
        assertEquals(ErrorCode.TEAM_PLAN_REQUIRED, exception.getErrorCode());
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 팀 소속인 사용자를 초대하면 예외가 발생한다")
    void inviteMemberThrowsWhenInviteeIsAlreadyMember() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        User invitee = createUser(2L, INVITEE_EMAIL, "초대 대상");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamMember manager = mock(TeamMember.class);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(INVITER_EMAIL)).thenReturn(Optional.of(inviter));
        when(teamPermissionService.requireManagePermission(TEAM_ID, inviter.getId()))
                .thenReturn(manager);
        when(teamMemberRepository.countByTeamId(TEAM_ID)).thenReturn(1L);
        when(userRepository.findByEmail(INVITEE_EMAIL)).thenReturn(Optional.of(invitee));
        when(teamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, invitee.getId()))
                .thenReturn(true);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.inviteMember(TEAM_ID, INVITER_EMAIL, INVITEE_EMAIL)
        );

        // then
        assertEquals(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적으로 팀 초대를 생성하면 초대가 저장된다")
    void inviteMemberSavesInvite() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamMember manager = mock(TeamMember.class);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(INVITER_EMAIL)).thenReturn(Optional.of(inviter));
        when(teamPermissionService.requireManagePermission(TEAM_ID, inviter.getId()))
                .thenReturn(manager);
        when(teamMemberRepository.countByTeamId(TEAM_ID)).thenReturn(1L);
        when(userRepository.findByEmail(INVITEE_EMAIL)).thenReturn(Optional.empty());
        when(teamInviteRepository.save(any(TeamInvite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TransactionSynchronizationManager.initSynchronization();

        try {
            // when
            teamService.inviteMember(TEAM_ID, INVITER_EMAIL, INVITEE_EMAIL);

            // then
            verify(teamInviteRepository).save(any(TeamInvite.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("팀 구성원 수가 한도에 도달하면 초대 시 예외가 발생한다")
    void inviteMemberThrowsWhenTeamMemberLimitIsReached() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamMember manager = mock(TeamMember.class);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(INVITER_EMAIL)).thenReturn(Optional.of(inviter));
        when(teamPermissionService.requireManagePermission(TEAM_ID, inviter.getId()))
                .thenReturn(manager);
        when(teamMemberRepository.countByTeamId(TEAM_ID)).thenReturn(10L);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.inviteMember(TEAM_ID, INVITER_EMAIL, INVITEE_EMAIL)
        );

        // then
        assertEquals(ErrorCode.TEAM_MEMBER_LIMIT_EXCEEDED, exception.getErrorCode());
    }

    @Test
    @DisplayName("만료된 초대 코드로 가입하면 예외가 발생한다")
    void joinTeamThrowsWhenInviteIsExpired() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        User member = createUser(2L, INVITEE_EMAIL, "가입자");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamInvite invite = TeamInvite.create(team, inviter, INVITEE_EMAIL);
        ReflectionTestUtils.setField(
                invite,
                "expiredAt",
                LocalDateTime.now().minusHours(1)
        );
        when(teamInviteRepository.findByCode(invite.getCode()))
                .thenReturn(Optional.of(invite));
        when(userRepository.findByEmail(INVITEE_EMAIL)).thenReturn(Optional.of(member));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.joinTeam(invite.getCode(), INVITEE_EMAIL)
        );

        // then
        assertEquals(ErrorCode.TEAM_INVITE_EXPIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 수락된 초대 코드로 가입하면 예외가 발생한다")
    void joinTeamThrowsWhenInviteIsAlreadyAccepted() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        User member = createUser(2L, INVITEE_EMAIL, "가입자");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamInvite invite = TeamInvite.create(team, inviter, INVITEE_EMAIL);
        ReflectionTestUtils.setField(invite, "accepted", true);
        when(teamInviteRepository.findByCode(invite.getCode()))
                .thenReturn(Optional.of(invite));
        when(userRepository.findByEmail(INVITEE_EMAIL)).thenReturn(Optional.of(member));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.joinTeam(invite.getCode(), INVITEE_EMAIL)
        );

        // then
        assertEquals(ErrorCode.TEAM_INVITE_ALREADY_ACCEPTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 초대 코드로 가입하면 팀 구성원이 추가된다")
    void joinTeamAddsTeamMember() {
        // given
        User inviter = createUser(1L, INVITER_EMAIL, "초대자");
        User member = createUser(2L, INVITEE_EMAIL, "가입자");
        Team team = createTeam(TEAM_ID, "개발팀", inviter);
        TeamInvite invite = TeamInvite.create(team, inviter, INVITEE_EMAIL);
        when(teamInviteRepository.findByCode(invite.getCode()))
                .thenReturn(Optional.of(invite));
        when(userRepository.findByEmail(INVITEE_EMAIL)).thenReturn(Optional.of(member));
        when(teamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, member.getId()))
                .thenReturn(false);
        when(teamMemberRepository.countByTeamId(TEAM_ID)).thenReturn(1L);

        // when
        teamService.joinTeam(invite.getCode(), INVITEE_EMAIL);

        // then
        assertTrue(invite.isAccepted());
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("팀 소유자가 스스로를 추방하려 하면 예외가 발생한다")
    void removeMemberThrowsWhenOwnerRemovesSelf() {
        // given
        Long memberId = 100L;
        User requester = createUser(1L, "owner@example.com", "팀 소유자");
        TeamMember requesterMember = mock(TeamMember.class);
        TeamMember targetMember = mock(TeamMember.class);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));
        when(teamPermissionService.requireManagePermission(TEAM_ID, requester.getId()))
                .thenReturn(requesterMember);
        when(teamMemberRepository.findByIdAndTeamId(memberId, TEAM_ID))
                .thenReturn(Optional.of(targetMember));
        when(targetMember.isOwner()).thenReturn(true);
        when(requesterMember.getId()).thenReturn(memberId);
        when(targetMember.getId()).thenReturn(memberId);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.removeMember(TEAM_ID, memberId, requester.getEmail())
        );

        // then
        assertEquals(ErrorCode.TEAM_OWNER_SELF_REMOVE_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 소유자가 스스로 탈퇴하려 하면 예외가 발생한다")
    void leaveTeamThrowsWhenOwnerLeaves() {
        // given
        User requester = createUser(1L, "owner@example.com", "팀 소유자");
        TeamMember ownerMember = mock(TeamMember.class);
        when(userRepository.findByEmail(requester.getEmail()))
                .thenReturn(Optional.of(requester));
        when(teamPermissionService.requireMembership(TEAM_ID, requester.getId()))
                .thenReturn(ownerMember);
        when(ownerMember.isOwner()).thenReturn(true);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> teamService.leaveTeam(TEAM_ID, requester.getEmail())
        );

        // then
        assertEquals(ErrorCode.TEAM_OWNER_SELF_REMOVE_DENIED, exception.getErrorCode());
    }

    private User createUser(Long id,
                            String email,
                            String name) {
        User user = User.create(
                email,
                name,
                "ROLE_USER",
                "google",
                "provider-id-" + id
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Team createTeam(Long id,
                            String name,
                            User owner) {
        Team team = Team.create(name, owner);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }
}