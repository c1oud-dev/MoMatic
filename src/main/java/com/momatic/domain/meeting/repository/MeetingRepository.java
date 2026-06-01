package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 회의 엔티티 저장소입니다. */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 소유자 이메일에 해당하는 회의 목록을 페이징 조회합니다.
     *
     * @param ownerEmail 소유자 이메일
     * @param pageable 페이징 정보
     * @return 소유자 회의 목록
     */
    Page<Meeting> findAllByOwnerEmail(String ownerEmail, Pageable pageable);

    /**
     * 소유자 이메일과 회의 ID로 회의를 조회합니다.
     *
     * @param id 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 조회된 회의
     */
    Optional<Meeting> findByIdAndOwnerEmail(Long id, String ownerEmail);

    /**
     * 소유자의 최근 회의 목록을 최대 5건 조회합니다.
     *
     * @param ownerEmail 소유자 이메일
     * @return 최근 회의 목록
     */
    List<Meeting> findTop5ByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
}
