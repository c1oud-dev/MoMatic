package com.momatic.domain.meeting.scheduler;

import com.momatic.domain.meeting.service.MeetingFileDeletionRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 삭제에 실패한 회의 업로드 파일을 주기적으로 재시도하는 스케줄러입니다. */
@Component
@RequiredArgsConstructor
public class MeetingFileDeletionScheduler {

    private final MeetingFileDeletionRetryService meetingFileDeletionRetryService;

    /** 10분마다 삭제 실패 파일의 삭제를 재시도합니다. */
    @Scheduled(cron = "0 */10 * * * *")
    public void retryFailedFileDeletions() {
        meetingFileDeletionRetryService.retryPending();
    }
}
