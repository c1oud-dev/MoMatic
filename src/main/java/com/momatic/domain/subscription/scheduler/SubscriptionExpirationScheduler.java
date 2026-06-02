package com.momatic.domain.subscription.scheduler;

import com.momatic.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 만료 시각이 지난 구독을 주기적으로 무료 플랜으로 전환하는 스케줄러입니다. */
@Component
@RequiredArgsConstructor
public class SubscriptionExpirationScheduler {

    private final SubscriptionService subscriptionService;

    /** 매일 자정에 만료 대상 구독을 일괄 처리합니다. */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        subscriptionService.expireSubscriptions();
    }
}