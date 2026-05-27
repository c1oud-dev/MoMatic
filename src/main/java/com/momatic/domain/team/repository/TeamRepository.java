package com.momatic.domain.team.repository;

import com.momatic.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByName(String name);
}
