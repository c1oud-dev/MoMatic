package com.momatic.domain.meeting.repository;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** 삭제 실패한 회의 업로드 파일 기록 저장소입니다. */
public interface FailedFileDeletionRepository extends JpaRepository<FailedFileDeletion, Long> {

    /**
     * 상태에 해당하는 파일 삭제 실패 기록 목록을 조회합니다.
     *
     * @param status 조회할 삭제 실패 상태
     * @return 파일 삭제 실패 기록 목록
     */
    List<FailedFileDeletion> findAllByStatus(FailedFileDeletionStatus status);
}