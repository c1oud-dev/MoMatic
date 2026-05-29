package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 회의 엔티티 저장소입니다. */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 소유자 이메일과 회의 ID로 회의를 조회합니다.
     *
     * @param id 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 조회된 회의
     */
    Optional<Meeting> findByIdAndOwnerEmail(Long id, String ownerEmail);

}
