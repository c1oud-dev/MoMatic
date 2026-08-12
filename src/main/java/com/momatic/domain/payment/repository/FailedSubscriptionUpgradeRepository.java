package com.momatic.domain.payment.repository;

import com.momatic.domain.payment.entity.FailedSubscriptionUpgrade;
import com.momatic.domain.payment.entity.FailedSubscriptionUpgradeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** 실패한 구독 업그레이드 기록 저장소입니다. */
public interface FailedSubscriptionUpgradeRepository
        extends JpaRepository<FailedSubscriptionUpgrade, Long> {

    /**
     * 상태에 해당하는 구독 업그레이드 실패 기록을 조회합니다.
     *
     * @param status 조회할 실패 기록 상태
     * @return 구독 업그레이드 실패 기록 목록
     */
    List<FailedSubscriptionUpgrade> findAllByStatus(
            FailedSubscriptionUpgradeStatus status
    );

    /**
     * 결제 ID와 상태에 해당하는 실패 기록이 존재하는지 확인합니다.
     *
     * @param paymentId 결제 ID
     * @param status 확인할 실패 기록 상태
     * @return 실패 기록 존재 여부
     */
    boolean existsByPaymentIdAndStatus(
            Long paymentId,
            FailedSubscriptionUpgradeStatus status
    );
}