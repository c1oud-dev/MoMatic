package com.momatic.domain.team.service;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.entity.TeamInvite;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.entity.TeamRole;
import com.momatic.domain.team.repository.TeamInviteRepository;
import com.momatic.domain.team.repository.TeamMemberRepository;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.mail.TeamInviteMailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 팀 생성, 초대, 구성원 권한 관리를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int MAX_TEAM_MEMBERS = 10;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final TeamInviteMailService teamInviteMailService;

    /**
     * 전체 팀 목록을 조회합니다.
     *
     * @return 팀 목록
     */
    @Transactional(readOnly = true)
    public List<Team> findTeams() {
        return teamRepository.findAll();
    }

    /**
     * 인증 사용자가 소속된 팀 목록을 조회합니다.
     *
     * @param memberEmail 인증 사용자 이메일
     * @return 소속 팀 목록
     */
    @Transactional(readOnly = true)
    public List<Team> findTeamsByMemberEmail(String memberEmail) {
        return teamMemberRepository.findAllByUserEmailOrderByCreatedAtAsc(memberEmail).stream()
                .map(TeamMember::getTeam)
                .toList();
    }

    /**
     * 팀 구성원 목록을 조회합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @return 팀 구성원 목록
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findMembers(Long teamId,
                                        String requesterEmail) {
        User requester = findUser(requesterEmail);
        validateMembership(teamId, requester.getId());
        return teamMemberRepository.findAllByTeamIdOrderByCreatedAtAsc(teamId);
    }

    /**
     * 팀에 대한 요청자의 구성원 정보를 조회합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @return 요청자 팀 구성원 정보
     */
    @Transactional(readOnly = true)
    public TeamMember findRequesterMember(Long teamId,
                                          String requesterEmail) {
        User requester = findUser(requesterEmail);
        return validateMembership(teamId, requester.getId());
    }

    /**
     * 팀 상세 정보를 조회합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @return 팀 정보
     */
    @Transactional(readOnly = true)
    public Team findTeamForMember(Long teamId,
                                  String requesterEmail) {
        User requester = findUser(requesterEmail);
        validateMembership(teamId, requester.getId());
        return findTeam(teamId);
    }

    /**
     * 팀을 생성하고 요청 사용자를 OWNER로 지정합니다.
     *
     * @param ownerEmail 팀 생성자 이메일
     * @param name 팀 이름
     * @return 생성된 팀
     */
    @Transactional
    public Team createTeam(String ownerEmail,
                           String name) {
        User owner = findUser(ownerEmail);
        validateTeamPlan(owner);
        validateRequiredText(name);
        return teamRepository.save(Team.create(name, owner));
    }

    /**
     * 팀 초대를 생성하고 이메일을 발송합니다.
     *
     * @param teamId 팀 ID
     * @param inviterEmail 초대자 이메일
     * @param inviteeEmail 초대 대상 이메일
     * @return 생성된 팀 초대
     */
    @Transactional
    public TeamInvite inviteMember(Long teamId,
                                   String inviterEmail,
                                   String inviteeEmail) {
        Team team = findTeam(teamId);
        User inviter = findUser(inviterEmail);
        validateManagePermission(teamId, inviter.getId());
        validateMemberLimit(teamId);
        validateRequiredText(inviteeEmail);
        User invitee = userRepository.findByEmail(inviteeEmail)
                .orElse(null);
        if (invitee != null && teamMemberRepository.existsByTeamIdAndUserId(teamId, invitee.getId())) {
            throw new CustomException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }

        TeamInvite invite = teamInviteRepository.save(TeamInvite.create(team, inviter, inviteeEmail));
        sendTeamInviteAfterCommit(invite);
        return invite;
    }

    /**
     * 초대 생성 트랜잭션 커밋 이후 팀 초대 메일을 발송합니다.
     *
     * @param invite 발송할 팀 초대
     */
    private void sendTeamInviteAfterCommit(TeamInvite invite) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /** 초대 생성 트랜잭션 커밋 이후 팀 초대 메일을 발송합니다. */
            @Override
            public void afterCommit() {
                teamInviteMailService.sendTeamInvite(invite);
            }
        });
    }

    /**
     * 초대 코드로 팀에 가입합니다.
     *
     * @param code 초대 코드
     * @param memberEmail 가입 사용자 이메일
     * @return 추가된 팀 구성원
     */
    @Transactional
    public TeamMember joinTeam(String code,
                               String memberEmail) {
        TeamInvite invite = teamInviteRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_INVITE_NOT_FOUND));
        User user = findUser(memberEmail);
        validateInvite(invite, user);
        validateMemberLimit(invite.getTeam().getId());

        TeamMember member = invite.getTeam().addMember(user, TeamRole.MEMBER);
        invite.accept();
        return teamMemberRepository.save(member);
    }

    /**
     * 팀 이름을 변경합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @param name 변경할 팀 이름
     */
    @Transactional
    public void updateTeamName(Long teamId,
                               String requesterEmail,
                               String name) {
        Team team = findTeam(teamId);
        User requester = findUser(requesterEmail);
        validateManagePermission(teamId, requester.getId());
        validateRequiredText(name);
        team.updateName(name);
    }

    /**
     * 팀 구성원 권한을 변경합니다.
     *
     * @param teamId 팀 ID
     * @param memberId 대상 구성원 ID
     * @param requesterEmail 요청자 이메일
     * @param role 변경할 권한 문자열
     * @return 변경된 팀 구성원
     */
    @Transactional
    public TeamMember updateMemberRole(Long teamId,
                                       Long memberId,
                                       String requesterEmail,
                                       String role) {
        User requester = findUser(requesterEmail);
        validateManagePermission(teamId, requester.getId());
        TeamMember target = findTeamMember(teamId, memberId);
        target.updateRole(TeamRole.from(role));
        return target;
    }

    /**
     * 팀 구성원을 추방합니다.
     *
     * @param teamId 팀 ID
     * @param memberId 대상 구성원 ID
     * @param requesterEmail 요청자 이메일
     */
    @Transactional
    public void removeMember(Long teamId,
                             Long memberId,
                             String requesterEmail) {
        User requester = findUser(requesterEmail);
        TeamMember requesterMember = validateManagePermission(teamId, requester.getId());
        TeamMember target = findTeamMember(teamId, memberId);
        if (target.isOwner() && requesterMember.getId().equals(target.getId())) {
            throw new CustomException(ErrorCode.TEAM_OWNER_SELF_REMOVE_DENIED);
        }
        teamMemberRepository.delete(target);
    }

    /**
     * 팀에서 탈퇴합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     */
    @Transactional
    public void leaveTeam(Long teamId,
                          String requesterEmail) {
        User requester = findUser(requesterEmail);
        TeamMember member = validateMembership(teamId, requester.getId());
        if (member.isOwner()) {
            throw new CustomException(ErrorCode.TEAM_OWNER_SELF_REMOVE_DENIED);
        }
        teamMemberRepository.delete(member);
    }

    /**
     * 사용자 활성 플랜이 팀 플랜인지 확인합니다.
     *
     * @param user 사용자
     */
    private void validateTeamPlan(User user) {
        if (subscriptionService.getActivePlan(user.getId()) != PlanPolicy.TEAM) {
            throw new CustomException(ErrorCode.TEAM_PLAN_REQUIRED);
        }
    }

    /**
     * 팀 소속 여부를 확인합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 요청자의 팀 구성원 정보
     */
    private TeamMember validateMembership(Long teamId,
                                          Long userId) {
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
    }

    /**
     * 팀 관리 권한을 확인합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 요청자의 팀 구성원 정보
     */
    private TeamMember validateManagePermission(Long teamId,
                                                Long userId) {
        TeamMember member = validateMembership(teamId, userId);
        if (!member.canManageTeam()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return member;
    }

    /**
     * 초대 정보를 검증합니다.
     *
     * @param invite 초대 정보
     * @param user 가입 사용자
     */
    private void validateInvite(TeamInvite invite,
                                User user) {
        if (invite.isAccepted()) {
            throw new CustomException(ErrorCode.TEAM_INVITE_ALREADY_ACCEPTED);
        }
        if (invite.isExpired()) {
            throw new CustomException(ErrorCode.TEAM_INVITE_EXPIRED);
        }
        if (!invite.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (teamMemberRepository.existsByTeamIdAndUserId(invite.getTeam().getId(), user.getId())) {
            throw new CustomException(ErrorCode.TEAM_MEMBER_ALREADY_EXISTS);
        }
    }

    /**
     * 팀 구성원 수 제한을 검증합니다.
     *
     * @param teamId 팀 ID
     */
    private void validateMemberLimit(Long teamId) {
        if (teamMemberRepository.countByTeamId(teamId) >= MAX_TEAM_MEMBERS) {
            throw new CustomException(ErrorCode.TEAM_MEMBER_LIMIT_EXCEEDED);
        }
    }

    /**
     * 필수 문자열 값을 검증합니다.
     *
     * @param text 검증할 문자열
     */
    private void validateRequiredText(String text) {
        if (text == null || text.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 사용자 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 팀 ID로 팀을 조회합니다.
     *
     * @param teamId 팀 ID
     * @return 팀 엔티티
     */
    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    /**
     * 팀 구성원을 조회합니다.
     *
     * @param teamId 팀 ID
     * @param memberId 구성원 ID
     * @return 팀 구성원
     */
    private TeamMember findTeamMember(Long teamId,
                                      Long memberId) {
        return teamMemberRepository.findByIdAndTeamId(memberId, teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
    }
}

