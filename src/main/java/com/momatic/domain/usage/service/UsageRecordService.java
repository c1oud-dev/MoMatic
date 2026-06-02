package com.momatic.domain.usage.service;

import com.momatic.domain.usage.repository.UsageRecordRepository;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 월별 사용량 기록 초기화를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;

    /** 현재 월 이전에 생성된 사용량 기록을 삭제합니다. */
    @Transactional
    public void deletePreviousMonthRecords() {
        usageRecordRepository.deleteByCreatedAtBefore(YearMonth.now().atDay(1).atStartOfDay());
    }
}
