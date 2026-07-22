package com.momatic.domain.usage.util;

import java.time.LocalDateTime;
import java.time.YearMonth;

/** 사용량 집계 기간을 표현하는 record입니다. */
public record UsagePeriod(LocalDateTime start, LocalDateTime end) {

    /**
     * 현재 월의 시작 시각 이상, 다음 월의 시작 시각 미만 범위를 생성합니다.
     *
     * @return 현재 월 사용량 집계 기간
     */
    public static UsagePeriod currentMonth() {
        YearMonth currentMonth = YearMonth.now();
        return new UsagePeriod(
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.plusMonths(1).atDay(1).atStartOfDay()
        );
    }
}