package com.momatic.domain.team.controller;

import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.domain.team.dto.TeamDashboardResponse;
import com.momatic.domain.team.dto.TeamMemberResponse;
import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.entity.TeamInvite;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.service.TeamDashboardService;
import com.momatic.domain.team.service.TeamService;
import java.util.List;

import com.momatic.global.error.CustomException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 팀 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamPageController {

    private static final String INVITE_RESULT_STATUS = "inviteResultStatus";
    private static final String INVITE_RESULT_TEAM_ID = "inviteResultTeamId";

    private final MeetingService meetingService;
    private final TeamService teamService;
    private final TeamDashboardService teamDashboardService;

    /**
     * 팀 목록 페이지를 표시합니다.
     *
     * @return 팀 목록 템플릿 경로
     */
    @GetMapping("/list")
    public String teamList() {
        return "team/team-list";
    }

    /**
     * 팀 생성 폼 페이지를 표시합니다.
     *
     * @return 팀 생성 폼 템플릿 경로
     */
    @GetMapping("/create")
    public String createTeamForm() {
        return "team/team-create";
    }

    /**
     * 팀 초대 정보를 검증하고 수락 또는 거절 확인 페이지를 표시합니다.
     *
     * @param code 초대 코드
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 팀 초대 확인 템플릿 경로
     */
    @GetMapping("/invite-confirm")
    public String inviteConfirm(@RequestParam(required = false) String code,
                                @AuthenticationPrincipal OAuth2User principal,
                                Model model) {
        try {
            TeamInvite invite = teamService.findInviteForConfirmation(code, getEmail(principal));
            model.addAttribute("teamName", invite.getTeam().getName());
            model.addAttribute("inviterName", invite.getInviter().getName());
            model.addAttribute("inviteeEmail", invite.getInviteeEmail());
            model.addAttribute("expireAt", invite.getExpiredAt());
            model.addAttribute("code", invite.getCode());
        } catch (CustomException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "team/team-invite-confirm";
    }

    /**
     * 팀 초대 수락 또는 거절 결과 페이지를 표시합니다.
     *
     * @param principal 인증 사용자 정보
     * @param session 인증 사용자 세션
     * @param model 화면 모델
     * @return 팀 초대 결과 템플릿 경로
     */
    @GetMapping("/invite-result")
    public String inviteResult(@AuthenticationPrincipal OAuth2User principal,
                               HttpSession session,
                               Model model) {
        String status = (String) session.getAttribute(INVITE_RESULT_STATUS);
        Long teamId = (Long) session.getAttribute(INVITE_RESULT_TEAM_ID);
        session.removeAttribute(INVITE_RESULT_STATUS);
        session.removeAttribute(INVITE_RESULT_TEAM_ID);

        if ("accepted".equals(status) && teamId != null) {
            try {
                TeamResponse team = TeamResponse.from(
                        teamService.findTeamForMember(teamId, getEmail(principal))
                );
                model.addAttribute("status", status);
                model.addAttribute("teamId", team.id());
                model.addAttribute("teamName", team.name());
                return "team/team-invite-result";
            } catch (CustomException exception) {
                model.addAttribute("status", "invalid");
                return "team/team-invite-result";
            }
        }
        model.addAttribute("status", "declined".equals(status) ? status : "invalid");
        return "team/team-invite-result";
    }

    /**
     * 팀 회의록 목록 페이지를 표시합니다.
     *
     * @param teamId 팀 ID
     * @param principal 인증 사용자 정보
     * @param pageable 페이징 정보
     * @param model 화면 모델
     * @return 팀 회의록 목록 템플릿 경로
     */
    @GetMapping("/{teamId}/meetings")
    public String teamMeetings(@PathVariable Long teamId,
                               @AuthenticationPrincipal OAuth2User principal,
                               @PageableDefault(size = 10) Pageable pageable,
                               Model model) {
        String requesterEmail = getEmail(principal);
        TeamMember requester = teamService.findRequesterMember(teamId, requesterEmail);
        Page<MeetingResponse> meetings = meetingService.findTeamMeetings(teamId, requesterEmail, pageable)
                .map(MeetingResponse::from);
        model.addAttribute("team", TeamResponse.from(teamService.findTeamForMember(teamId, requesterEmail)));
        model.addAttribute("meetings", meetings);
        model.addAttribute("canManage", requester.canManageTeam());
        return "meeting/list";
    }

    /**
     * 팀 대시보드 페이지를 표시합니다.
     *
     * @param teamId 팀 ID
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 팀 대시보드 템플릿 경로
     */
    @GetMapping("/{teamId}/dashboard")
    public String teamDashboard(@PathVariable Long teamId,
                                @AuthenticationPrincipal OAuth2User principal,
                                Model model) {
        String requesterEmail = getEmail(principal);
        TeamMember requester = teamService.findRequesterMember(teamId, requesterEmail);
        TeamDashboardResponse dashboard = teamDashboardService.getDashboard(teamId, requesterEmail);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("canManage", requester.canManageTeam());
        return "team/team-dashboard";
    }

    /**
     * 팀 멤버 관리 페이지를 표시합니다.
     *
     * @param teamId 팀 ID
     * @param principal 인증 사용자 정보
     * @param model 화면 모델
     * @return 팀 멤버 관리 템플릿 경로
     */
    @GetMapping("/{teamId}/members")
    public String teamMembers(@PathVariable Long teamId,
                              @AuthenticationPrincipal OAuth2User principal,
                              Model model) {
        TeamMember requester = teamService.findRequesterMember(teamId, getEmail(principal));
        List<TeamMemberResponse> members = teamService.findMembers(teamId, getEmail(principal)).stream()
                .map(TeamMemberResponse::from)
                .toList();
        model.addAttribute("team", TeamResponse.from(teamService.findTeamForMember(teamId, getEmail(principal))));
        model.addAttribute("members", members);
        model.addAttribute("requesterRole", requester.getRole().name());
        model.addAttribute("canManage", requester.canManageTeam());
        return "team/team-members";
    }

    /**
     * 인증 사용자 정보에서 이메일을 조회합니다.
     *
     * @param principal 인증 사용자 정보
     * @return 인증 사용자 이메일
     */
    private String getEmail(OAuth2User principal) {
        return principal.getAttribute("email");
    }
}