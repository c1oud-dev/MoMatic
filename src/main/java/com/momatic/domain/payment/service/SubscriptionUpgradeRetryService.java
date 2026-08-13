package com.momatic.domain.payment.service;

import com.momatic.domain.payment.entity.FailedSubscriptionUpgrade;
import com.momatic.domain.payment.entity.FailedSubscriptionUpgradeStatus;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.entity.PaymentStatus;
import com.momatic.domain.payment.repository.FailedSubscriptionUpgradeRepository;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 실패한 구독 업그레이드를 기록하고 주기적으로 재시도하는 서비스입니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionUpgradeRetryService {

    private static final int MAX_RETRY_COUNT = 5;

    private final FailedSubscriptionUpgradeRepository failedSubscriptionUpgradeRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 구독 업그레이드 실패를 독립 트랜잭션으로 재시도 큐에 기록합니다.
     *
     * @param paymentId 결제 ID
     * @param errorMessage 실패 원인 메시지
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long paymentId,
                              String errorMessage) {
        if (failedSubscriptionUpgradeRepository.existsByPaymentIdAndStatus(
                paymentId,
                FailedSubscriptionUpgradeStatus.PENDING
        )) {
            return;
        }

        FailedSubscriptionUpgrade failure = FailedSubscriptionUpgrade.create(paymentId);
        failure.recordInitialError(errorMessage);
        failedSubscriptionUpgradeRepository.save(failure);
        log.warn("구독 업그레이드 실패를 재시도 큐에 기록했습니다: paymentId={}", paymentId);
    }

    /** 구독 업그레이드 재시도 대기 기록을 조회하여 처리합니다. */
    @Transactional
    public void retryPending() {
        failedSubscriptionUpgradeRepository
                .findAllByStatus(FailedSubscriptionUpgradeStatus.PENDING)
                .forEach(this::retry);
    }

    /**
     * 구독 업그레이드 실패 기록 한 건을 재시도합니다.
     *
     * @param failure 구독 업그레이드 실패 기록
     */
    private void retry(FailedSubscriptionUpgrade failure) {
        Payment payment = paymentRepository.findById(failure.getPaymentId()).orElse(null);
        if (payment == null) {
            failure.markResolved();
            log.info(
                    "결제가 존재하지 않아 구독 업그레이드 재시도 기록을 제거합니다: paymentId={}",
                    failure.getPaymentId()
            );
            return;
        }
        if (payment.getStatus() != PaymentStatus.DONE) {
            failure.markResolved();
            log.info(
                    "완료 결제가 아니므로 구독 업그레이드 재시도 기록을 제거합니다: "
                            + "paymentId={}, status={}",
                    payment.getId(),
                    payment.getStatus()
            );
            return;
        }

        try {
            subscriptionService.upgrade(
                    payment.getUser().getId(),
                    payment.getPlanType()
            );
            failure.markResolved();
            log.info("구독 업그레이드 재시도에 성공했습니다: paymentId={}", payment.getId());
        } catch (RuntimeException exception) {
            failure.recordFailedAttempt(MAX_RETRY_COUNT, exception.getMessage());
            log.error("구독 업그레이드 재시도에 실패했습니다: paymentId={}",
                    payment.getId(),
                    exception);
            if (failure.getStatus() == FailedSubscriptionUpgradeStatus.GIVEN_UP) {
                log.error(
                        "구독 업그레이드 최대 재시도 횟수에 도달하여 수동 확인이 필요합니다: "
                                + "paymentId={}, retryCount={}",
                        payment.getId(),
                        failure.getRetryCount()
                );
            }
        }
    }
}