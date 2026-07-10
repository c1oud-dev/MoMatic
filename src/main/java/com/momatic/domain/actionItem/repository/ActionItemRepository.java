package com.momatic.domain.actionItem.repository;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.entity.ActionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 액션 아이템 엔티티 저장소입니다. */
public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    /**
     * 액션 아이템 ID로 회의 정보를 함께 조회합니다.
     *
     * @param id 액션 아이템 ID
     * @return 조회된 액션 아이템
     */
    @Override
    @EntityGraph(attributePaths = {"meeting", "meeting.team", "meeting.owner"})
    Optional<ActionItem> findById(Long id);

    /**
     * 회의 ID에 해당하는 액션 아이템 목록을 조회합니다.
     *
     * @param meetingId 회의 ID
     * @return 액션 아이템 목록
     */
    List<ActionItem> findByMeetingId(Long meetingId);

    /**
     * 소유자 이메일과 액션 아이템 ID로 액션 아이템을 조회합니다.
     *
     * @param id 액션 아이템 ID
     * @param ownerEmail 회의 소유자 이메일
     * @return 조회된 액션 아이템
     */
    Optional<ActionItem> findByIdAndMeetingOwnerEmail(Long id, String ownerEmail);

    /**
     * 소유자의 지정 상태 액션 아이템을 최근 순으로 최대 5건 조회합니다.
     *
     * @param ownerEmail 회의 소유자 이메일
     * @param statuses 조회할 액션 아이템 상태 목록
     * @return 미완료 액션 아이템 목록
     */
    List<ActionItem> findTop5ByMeetingOwnerEmailAndStatusInOrderByCreatedAtDesc(String ownerEmail,
                                                                                List<ActionStatus> statuses);

    /**
     * 소유자 이메일로 전체 액션아이템을 페이징 조회합니다.
     *
     * @param ownerEmail 회의 소유자 이메일
     * @param pageable 페이징 정보
     * @return 액션 아이템 페이지
     */
    @EntityGraph(attributePaths = {"meeting"})
    Page<ActionItem> findByMeetingOwnerEmail(String ownerEmail, Pageable pageable);

    /**
     * 소유자 이메일과 상태로 액션아이템을 페이징 조회합니다.
     *
     * @param ownerEmail 회의 소유자 이메일
     * @param status 액션 아이템 상태
     * @param pageable 페이징 정보
     * @return 액션 아이템 페이지
     */
    @EntityGraph(attributePaths = {"meeting"})
    Page<ActionItem> findByMeetingOwnerEmailAndStatus(String ownerEmail, ActionStatus status, Pageable pageable);

    /**
     * 팀의 지정 상태 액션 아이템 수를 조회합니다.
     *
     * @param teamId 팀 ID
     * @param statuses 조회할 액션 아이템 상태 목록
     * @return 액션 아이템 수
     */
    long countByMeetingTeamIdAndStatusIn(Long teamId,
                                         Collection<ActionStatus> statuses);

    /**
     * 팀의 전체 액션 아이템 수를 조회합니다.
     *
     * @param teamId 팀 ID
     * @return 액션 아이템 수
     */
    long countByMeetingTeamId(Long teamId);
}
