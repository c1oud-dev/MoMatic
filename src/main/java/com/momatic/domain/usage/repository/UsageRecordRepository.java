package com.momatic.domain.usage.repository;

import com.momatic.domain.usage.entity.UsageRecord;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 사용량 기록 조회를 위한 레포지토리입니다. */
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    /**
     * 특정 기간 내 업로드 사용 횟수를 반개구간으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @param usageType 사용 타입
     * @param from 시작 시각
     * @param to 종료 시각 미만
     */
    long countByUserIdAndUsageTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Long userId,
                                                                                   String usageType,
                                                                                   LocalDateTime from,
                                                                                   LocalDateTime to);

    /**
     * 특정 기간 내 업로드 파일 용량 합계를 반개구간으로 조회합니다
     *
     * @param userId 사용자 ID
     * @param usageType 사용 타입
     * @param from 시작 시각 이상
     * @param to 종료 시각 미만
     * @return 업로드 파일 바이트 합계
     */
    @Query("""
            select coalesce(sum(usageRecord.fileSizeBytes), 0)
            from UsageRecord usageRecord
            where usageRecord.user.id = :userId
              and usageRecord.usageType = :usageType
              and usageRecord.createdAt >= :from
              and usageRecord.createdAt < :to
            """)
    long sumFileSizeBytes(@Param("userId") Long userId,
                          @Param("usageType") String usageType,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

    /**
     * 지정 시각 이전의 사용량 기록을 삭제합니다.
     *
     * @param before 삭제 기준 시각 미만
     */
    void deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * 사용자 ID에 해당하는 사용량 기록을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
}
