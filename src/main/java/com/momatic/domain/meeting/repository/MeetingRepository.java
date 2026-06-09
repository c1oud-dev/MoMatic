package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 회의 엔티티 저장소입니다. */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 회의 ID로 회의를 팀 및 소유자와 함께 조회합니다.
     *
     * @param id 회의 ID
     * @return 조회된 회의
     */
    @Override
    @EntityGraph(attributePaths = {"team", "owner"})
    Optional<Meeting> findById(Long id);

    /**
     * 소유자 이메일에 해당하는 개인 회의 목록을 페이징 조회합니다.
     *
     * @param ownerEmail 소유자 이메일
     * @param pageable 페이징 정보
     * @return 소유자 개인 회의 목록
     */
    @EntityGraph(attributePaths = {"team"})
    Page<Meeting> findAllByOwnerEmailAndTeamIsNull(String ownerEmail, Pageable pageable);

    /**
     * 소유자 이메일과 회의 ID로 회의를 조회합니다.
     *
     * @param id 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 조회된 회의
     */
    @EntityGraph(attributePaths = {"team", "owner"})
    Optional<Meeting> findByIdAndOwnerEmail(Long id, String ownerEmail);

    /**
     * 소유자의 최근 회의 목록을 최대 5건 조회합니다.
     *
     * @param ownerEmail 소유자 이메일
     * @return 최근 회의 목록
     */
    @EntityGraph(attributePaths = {"team"})
    List<Meeting> findTop5ByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);

    /**
     * 팀 회의 목록을 페이징 조회합니다.
     *
     * @param teamId 팀 ID
     * @param pageable 페이징 정보
     * @return 팀 회의 목록
     */
    @EntityGraph(attributePaths = {"team", "owner"})
    Page<Meeting> findAllByTeamId(Long teamId, Pageable pageable);

    /**
     * 팀 전체 회의 수를 조회합니다.
     *
     * @param teamId 팀 ID
     * @return 팀 전체 회의 수
     */
    long countByTeamId(Long teamId);

    /**
     * 팀 구성원이 업로드한 팀 회의 수를 조회합니다.
     *
     * @param teamId 팀 ID
     * @param ownerId 업로드 사용자 ID
     * @return 업로드한 회의 수
     */
    long countByTeamIdAndOwnerId(Long teamId,
                                 Long ownerId);
}
