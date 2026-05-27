package com.momatic.domain.team.repository;

import com.momatic.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 팀 엔티티 저장소입니다.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 팀 이름으로 팀을 조회합니다.
     *
     * @param name 팀 이름
     * @return 조회된 팀
     */
    Optional<Team> findByName(String name);
}
