package com.momatic.domain.team.controller;

import com.momatic.domain.team.dto.TeamResponse;
import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.repository.TeamRepository;
import com.momatic.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 팀 조회 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;

    /**
     * 팀 목록을 조회합니다.
     *
     * @return 팀 목록 응답
     */
    @GetMapping
    public ApiResponse<List<TeamResponse>> listTeams() {
        List<TeamResponse> teams = teamRepository.findAll().stream()
                .map(TeamResponse::from)
                .toList();
        return ApiResponse.ok(teams);
    }
}
