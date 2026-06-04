package com.momatic.domain.team.repository;

import com.momatic.domain.team.entity.TeamInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 팀 초대 엔티티 저장소입니다. */
public interface TeamInviteRepository extends JpaRepository<TeamInvite, Long> {

    /**
     * 초대 코드로 팀 초대를 조회합니다.
     *
     * @param code 초대 코드
     * @return 조회된 팀 초대
     */
    Optional<TeamInvite> findByCode(String code);
}
