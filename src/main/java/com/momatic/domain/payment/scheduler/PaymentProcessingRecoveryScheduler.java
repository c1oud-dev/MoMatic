package com.momatic.domain.payment.scheduler;

import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.service.PaymentConfirmProcessor;
import com.momatic.infra.toss.TossPaymentClient;
import com.momatic.infra.toss.TossPaymentResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 장시간 처리 중인 결제를 토스 상태와 대조하여 복구하는 스케줄러입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessingRecoveryScheduler {

    private static final long STALE_THRESHOLD_MINUTES = 10L;

    private final PaymentConfirmProcessor paymentConfirmProcessor;
    private final TossPaymentClient tossPaymentClient;

    /** 5분마다 정체된 처리 중 결제를 복구합니다. */
    @Scheduled(fixedDelay = 300_000L)
    public void recoverStalePayments() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(STALE_THRESHOLD_MINUTES);
        paymentConfirmProcessor.findStaleProcessingPayments(threshold)
                .forEach(this::recoverPayment);
    }

    /**
     * 정체 결제 한 건을 토스 조회 결과에 따라 완료하거나 대기 상태로 복구합니다.
     *
     * @param payment 정체된 결제
     */
    private void recoverPayment(Payment payment) {
        try {
            Optional<TossPaymentResponse> response = tossPaymentClient.findByOrderId(
                    payment.getOrderId()
            );
            if (response.isPresent() && "DONE".equals(response.get().status())) {
                Payment completed = paymentConfirmProcessor.completeRecovered(
                        payment.getId(),
                        response.get()
                );
                log.info(
                        "정체 결제를 승인 완료 상태로 확정했습니다: paymentId={}, orderId={}",
                        completed.getId(),
                        completed.getOrderId()
                );
                upgradeSubscriptionSafely(completed.getId());
                return;
            }
            log.info(
                    "정체 결제를 승인 대기 상태로 복구합니다: paymentId={}, orderId={}, tossStatus={}",
                    payment.getId(),
                    payment.getOrderId(),
                    response.map(TossPaymentResponse::status).orElse("NOT_FOUND")
            );
            paymentConfirmProcessor.restoreToPending(payment.getId());
        } catch (RuntimeException exception) {
            log.error(
                    "정체 결제 복구에 실패하여 다음 주기에 재시도합니다: paymentId={}",
                    payment.getId(),
                    exception
            );
        }
    }

    /**
     * 복구 완료된 결제의 구독을 업그레이드하고 실패를 기록합니다.
     *
     * @param paymentId 결제 ID
     */
    private void upgradeSubscriptionSafely(Long paymentId) {
        try {
            paymentConfirmProcessor.upgradeSubscription(paymentId);
        } catch (RuntimeException exception) {
            // TODO 실패한 구독 업그레이드를 재시도 큐에 등록합니다.
            log.error(
                    "정체 결제 복구 후 구독 업그레이드에 실패했습니다: paymentId={}",
                    paymentId,
                    exception
            );
        }
    }
}