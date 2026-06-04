package com.momatic.domain.usage.scheduler;

import com.momatic.domain.usage.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 매월 이전 사용량 기록을 초기화하는 스케줄러입니다. */
@Component
@RequiredArgsConstructor
public class UsageRecordResetScheduler {

    private final UsageRecordService usageRecordService;

    /** 매월 1일 자정에 전월 이하 사용량 기록을 삭제합니다. */
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetPreviousMonthRecords() {
        usageRecordService.deletePreviousMonthRecords();
    }
}
