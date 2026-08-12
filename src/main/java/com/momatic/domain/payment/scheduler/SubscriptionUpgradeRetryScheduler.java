package com.momatic.domain.payment.scheduler;

import com.momatic.domain.payment.service.SubscriptionUpgradeRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 실패한 구독 업그레이드를 주기적으로 재시도하는 스케줄러입니다. */
@Component
@RequiredArgsConstructor
public class SubscriptionUpgradeRetryScheduler {

    private final SubscriptionUpgradeRetryService subscriptionUpgradeRetryService;

    /** 사용자가 즉시 체감하는 구독 미반영을 줄이기 위해 1분마다 재시도합니다. */
    @Scheduled(cron = "0 */1 * * * *")
    public void retryFailedSubscriptionUpgrades() {
        subscriptionUpgradeRetryService.retryPending();
    }
}
