package com.momatic.domain.usage.service;

import com.momatic.domain.usage.repository.UsageRecordRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 월별 사용량 기록 초기화를 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final UserRepository userRepository;

    /** 현재 월 이전에 생성된 사용량 기록을 삭제합니다. */
    @Transactional
    public void deletePreviousMonthRecords() {
        usageRecordRepository.deleteByCreatedAtBefore(YearMonth.now().atDay(1).atStartOfDay());
    }

    /**
     * 이메일에 해당하는 사용자의 이번 달 업로드 횟수를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 이번 달 업로드 횟수
     */
    @Transactional(readOnly = true)
    public long getMonthlyUploadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime from = LocalDateTime.of(currentMonth.atDay(1), LocalTime.MIDNIGHT);
        LocalDateTime to = LocalDateTime.of(currentMonth.plusMonths(1).atDay(1), LocalTime.MIDNIGHT);
        return usageRecordRepository.countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                user.getId(),
                "UPLOAD",
                from,
                to
        );
    }
}
