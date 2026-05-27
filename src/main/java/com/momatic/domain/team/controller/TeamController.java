package com.momatic.domain.team.controller;

import com.momatic.domain.team.entity.Team;
import com.momatic.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepo;

    @GetMapping
    public List<Team> listTeams() {
        return teamRepo.findAll();
    }
}
