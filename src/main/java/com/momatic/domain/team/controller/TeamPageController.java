package com.momatic.domain.team.controller;

import com.momatic.domain.meeting.dto.MeetingResponse;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.domain.team.dto.TeamDashboardResponse;
import com.momatic.domain.team.dto.TeamMemberResponse;
import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.service.TeamDashboardService;
import com.momatic.domain.team.service.TeamService;
import java.util.List;
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

/** 팀 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamPageController {

    private final MeetingService meetingService;
    private final TeamService teamService;
    private final TeamDashboardService teamDashboardService;

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
        Page<MeetingResponse> meetings = meetingService.findTeamMeetings(teamId, getEmail(principal), pageable)
                .map(MeetingResponse::from);
        model.addAttribute("team", TeamResponse.from(teamService.findTeamForMember(teamId, getEmail(principal))));
        model.addAttribute("meetings", meetings);
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
        TeamDashboardResponse dashboard = teamDashboardService.getDashboard(teamId, getEmail(principal));
        model.addAttribute("dashboard", dashboard);
        return "team-dashboard";
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
        return "team-members";
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