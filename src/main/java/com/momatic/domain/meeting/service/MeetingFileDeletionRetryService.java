package com.momatic.domain.meeting.service;

import com.momatic.domain.meeting.entity.FailedFileDeletion;
import com.momatic.domain.meeting.entity.FailedFileDeletionStatus;
import com.momatic.domain.meeting.repository.FailedFileDeletionRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
     *
     * @param storedFileName 저장 파일명
     */
    @Transactional
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
                        failedFileDeletion.recordFailedAttempt(MAX_RETRY_COUNT);
                    }
                });
    }
}
