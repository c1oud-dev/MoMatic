package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import com.momatic.domain.meeting.repository.FailedFileDeletionRepository;
import java.io.IOException;

import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 삭제 실패한 회의 업로드 파일을 기록하고 재시도하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class MeetingFileDeletionRetryService {

    private static final int MAX_RETRY_COUNT = 5;

    private final FailedFileDeletionRepository failedFileDeletionRepository;
    private final MeetingFileStorageService meetingFileStorageService;

    /**
     * 삭제 실패한 저장 파일명을 재시도 큐에 기록합니다.
     * afterCommit 콜백 등 원래 트랜잭션이 이미 종료된 시점에서 호출될 수 있으므로,
     * 독립적인 새 트랜잭션에서 확실히 커밋되도록 REQUIRES_NEW로 전파합니다.
     *
     * @param storedFileName 저장 파일명
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String storedFileName) {
        failedFileDeletionRepository.save(FailedFileDeletion.create(storedFileName));
    }

    /** 삭제 대기 중인 파일 삭제 실패 기록을 조회하여 삭제를 재시도합니다. */
    @Transactional
    public void retryPending() {
        failedFileDeletionRepository.findAllByStatus(FailedFileDeletionStatus.PENDING)
                .forEach(failedFileDeletion -> {
                    try {
                        meetingFileStorageService.deleteFile(failedFileDeletion.getStoredFileName());
                        failedFileDeletion.markResolved();
                    } catch (IOException exception) {
                        failedFileDeletion.recordFailedAttempt(
                                MAX_RETRY_COUNT,
                                exception.getMessage()
                        );
                    }
                });
    }

    /**
     * 파일 삭제 재시도 기록을 페이지 단위로 조회합니다.
     *
     * @param status 선택한 상태, 전체 조회 시 {@code null}
     * @param pageable 페이지 요청 정보
     * @return 파일 삭제 재시도 기록 페이지
     */
    @Transactional(readOnly = true)
    public Page<FailedFileDeletion> findAll(FailedFileDeletionStatus status,
                                            Pageable pageable) {
        if (status == null) {
            return failedFileDeletionRepository.findAll(pageable);
        }
        return failedFileDeletionRepository.findAllByStatus(status, pageable);
    }

    /**
     * 상태에 해당하는 파일 삭제 재시도 기록 수를 조회합니다.
     *
     * @param status 집계할 상태
     * @return 상태별 기록 수
     */
    @Transactional(readOnly = true)
    public long countByStatus(FailedFileDeletionStatus status) {
        return failedFileDeletionRepository.countByStatus(status);
    }

    /**
     * 포기된 파일 삭제 기록을 수동 재시도 대기 상태로 되돌립니다.
     *
     * @param id 파일 삭제 기록 ID
     */
    @Transactional
    public void resetForManualRetry(Long id) {
        FailedFileDeletion failedFileDeletion = failedFileDeletionRepository.findLockedById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_DELETION_NOT_FOUND));
        if (failedFileDeletion.getStatus() == FailedFileDeletionStatus.RESOLVED) {
            throw new CustomException(ErrorCode.RESOLVED_FILE_DELETION_RETRY_NOT_ALLOWED);
        }
        if (failedFileDeletion.getStatus() != FailedFileDeletionStatus.GIVEN_UP) {
            throw new CustomException(ErrorCode.FILE_DELETION_RETRY_NOT_GIVEN_UP);
        }
        failedFileDeletion.resetForManualRetry();
    }
}
