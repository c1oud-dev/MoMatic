package com.momatic.domain.team.repository;

import com.momatic.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 팀 구성원 엔티티 저장소입니다. */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * 팀과 사용자 ID로 팀 구성원을 조회합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 조회된 팀 구성원
     */
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId,
                                               Long userId);

    /**
     * 구성원 ID와 팀 ID로 팀 구성원을 사용자와 함께 조회합니다.
     *
     * @param id 구성원 ID
     * @param teamId 팀 ID
     * @return 조회된 팀 구성원
     */
    @EntityGraph(attributePaths = {"team", "user"})
    Optional<TeamMember> findByIdAndTeamId(Long id,
                                           Long teamId);

    /**
     * 팀과 사용자 이메일로 팀 구성원을 조회합니다.
     *
     * @param teamId 팀 ID
     * @param email 사용자 이메일
     * @return 조회된 팀 구성원
     */
    Optional<TeamMember> findByTeamIdAndUserEmail(Long teamId,
                                                  String email);

    /**
     * 팀 구성원 수를 조회합니다.
     *
     * @param teamId 팀 ID
     * @return 팀 구성원 수
     */
    long countByTeamId(Long teamId);

    /**
     * 팀과 사용자 ID에 해당하는 구성원 존재 여부를 확인합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 구성원 존재 여부
     */
    boolean existsByTeamIdAndUserId(Long teamId,
                                    Long userId);
}

