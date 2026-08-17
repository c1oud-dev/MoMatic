package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/** 삭제 실패한 회의 업로드 파일 기록 저장소입니다. */
public interface FailedFileDeletionRepository extends JpaRepository<FailedFileDeletion, Long> {

    /**
     * 상태에 해당하는 파일 삭제 실패 기록 목록을 조회합니다.
     *
     * @param status 조회할 삭제 실패 상태
     * @return 파일 삭제 실패 기록 목록
     */
    List<FailedFileDeletion> findAllByStatus(FailedFileDeletionStatus status);

    /**
     * 상태에 해당하는 기록을 페이지 단위로 조회합니다.
     *
     * @param status 조회할 삭제 실패 상태
     * @param pageable 페이지 요청 정보
     * @return 파일 삭제 실패 기록 페이지
     */
    Page<FailedFileDeletion> findAllByStatus(FailedFileDeletionStatus status,
                                             Pageable pageable);

    /**
     * 상태별 파일 삭제 기록 수를 조회합니다.
     *
     * @param status 집계할 삭제 실패 상태
     * @return 상태별 기록 수
     */
    long countByStatus(FailedFileDeletionStatus status);

    /**
     * 관리자 수동 재시도 변경을 위해 기록을 배타 잠금으로 조회합니다.
     *
     * @param id 파일 삭제 기록 ID
     * @return 잠긴 파일 삭제 기록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FailedFileDeletion> findLockedById(Long id);
}