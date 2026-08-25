package com.momatic.domain.payment.scheduler;

import com.momatic.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 생성 후 장시간 승인되지 않은 결제를 만료 처리하는 스케줄러입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentExpirationScheduler {

    private final PaymentService paymentService;

    /** 10분마다 생성 후 한 시간이 지난 승인 대기 결제를 만료 처리합니다. */
    @Scheduled(fixedDelay = 600_000L)
    public void expirePendingPayments() {
        log.debug("승인 대기 결제 만료 처리를 시작합니다.");
        paymentService.expirePendingPayments();
    }
}