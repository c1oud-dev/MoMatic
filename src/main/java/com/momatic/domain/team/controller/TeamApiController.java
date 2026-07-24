package com.momatic.domain.team.controller;

import com.momatic.domain.team.dto.*;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.service.TeamService;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 팀 생성, 초대, 구성원 관리 API 컨트롤러입니다. */
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamApiController {

    private final TeamService teamService;

    /**
     * 팀 목록을 조회합니다.
     *
     * @return 팀 목록 응답
     */
    @GetMapping
    public ApiResponse<List<TeamResponse>> listTeams() {
        List<TeamResponse> teams = teamService.findTeams().stream()
                .map(TeamResponse::from)
                .toList();
        return ApiResponse.ok(teams);
    }

    /**
     * 팀을 생성합니다.
     *
     * @param request 팀 생성 요청
     * @param principal 인증 사용자 정보
     * @return 생성된 팀 응답
     */
    @PostMapping
    public ApiResponse<TeamResponse> createTeam(@Valid @RequestBody TeamCreateRequest request,
                                                @AuthenticationPrincipal OAuth2User principal) {
        Team team = teamService.createTeam(
                AuthenticatedUserResolver.getEmail(principal),
                request.name()
        );
        return ApiResponse.ok(TeamResponse.from(team));
    }

    /**
     * 팀 구성원을 초대합니다.
     *
     * @param teamId 팀 ID
     * @param request 팀 초대 요청
     * @param principal 인증 사용자 정보
     * @return 생성된 팀 초대 응답
     */
    @PostMapping("/{teamId}/invites")
    public ApiResponse<TeamInviteResponse> inviteMember(@PathVariable Long teamId,
                                                        @Valid @RequestBody TeamInviteRequest request,
                                                        @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(TeamInviteResponse.from(
                teamService.inviteMember(
                        teamId,
                        AuthenticatedUserResolver.getEmail(principal),
                        request.email()
                )
        ));
    }

    /**
     * 초대 코드로 팀에 가입합니다.
     *
     * @param code 초대 코드
     * @param principal 인증 사용자 정보
     * @return 추가된 팀 구성원 응답
     */
    @GetMapping("/join")
    public ApiResponse<TeamMemberResponse> joinTeam(@RequestParam String code,
                                                    @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(TeamMemberResponse.from(
                teamService.joinTeam(code, AuthenticatedUserResolver.getEmail(principal))
        ));
    }

    /**
     * 팀 이름을 변경합니다.
     *
     * @param teamId 팀 ID
     * @param request 팀 이름 변경 요청
     * @param principal 인증 사용자 정보
     * @return 변경 완료 응답
     */
    @PatchMapping("/{teamId}/name")
    public ApiResponse<Void> updateTeamName(@PathVariable Long teamId,
                                            @Valid @RequestBody TeamNameUpdateRequest request,
                                            @AuthenticationPrincipal OAuth2User principal) {
        teamService.updateTeamName(
                teamId,
                AuthenticatedUserResolver.getEmail(principal),
                request.name()
        );
        return ApiResponse.ok(null);
    }

    /**
     * 팀 구성원 권한을 변경합니다.
     *
     * @param teamId 팀 ID
     * @param memberId 구성원 ID
     * @param request 팀 권한 변경 요청
     * @param principal 인증 사용자 정보
     * @return 변경된 팀 구성원 응답
     */
    @PatchMapping("/{teamId}/members/{memberId}/role")
    public ApiResponse<TeamMemberResponse> updateMemberRole(@PathVariable Long teamId,
                                                            @PathVariable Long memberId,
                                                            @Valid @RequestBody TeamRoleUpdateRequest request,
                                                            @AuthenticationPrincipal OAuth2User principal) {
        return ApiResponse.ok(TeamMemberResponse.from(
                teamService.updateMemberRole(
                        teamId,
                        memberId,
                        AuthenticatedUserResolver.getEmail(principal),
                        request.role()
                )
        ));
    }

    /**
     * 팀 구성원을 추방합니다.
     *
     * @param teamId 팀 ID
     * @param memberId 구성원 ID
     * @param principal 인증 사용자 정보
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ApiResponse<Void> removeMember(@PathVariable Long teamId,
                                          @PathVariable Long memberId,
                                          @AuthenticationPrincipal OAuth2User principal) {
        teamService.removeMember(
                teamId,
                memberId,
                AuthenticatedUserResolver.getEmail(principal)
        );
        return ApiResponse.ok(null);
    }

    /**
     * 팀에서 탈퇴합니다.
     *
     * @param teamId 팀 ID
     * @param principal 인증 사용자 정보
     * @return 탈퇴 완료 응답
     */
    @DeleteMapping("/{teamId}/leave")
    public ApiResponse<Void> leaveTeam(@PathVariable Long teamId,
                                       @AuthenticationPrincipal OAuth2User principal) {
        teamService.leaveTeam(
                teamId,
                AuthenticatedUserResolver.getEmail(principal)
        );
        return ApiResponse.ok(null);
    }
}
