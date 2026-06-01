package com.momatic.domain.usage.repository;

import com.momatic.domain.usage.entity.UsageRecord;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용량 기록 조회를 위한 레포지토리입니다. */
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    /**
     * 특정 기간 내 업로드 사용 횟수를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param usageType 사용 타입
     * @param from 시작 시각
     * @param to 종료 시각
     * @return 사용 횟수
     */
    long countByUserIdAndUsageTypeAndCreatedAtBetween(Long userId,
                                                      String usageType,
                                                      LocalDateTime from,
                                                      LocalDateTime to);

    /**
     * 특정 기간 내 업로드 사용 횟수를 반개구간으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @param usageType 사용 타입
     * @param from 시작 시각 이상
     * @param to 종료 시각 미만
     * @return 사용 횟수
     */
    long countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Long userId,
                                                                                   String usageType,
                                                                                   LocalDateTime from,
                                                                                   LocalDateTime to);
}
