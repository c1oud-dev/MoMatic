package com.momatic.domain.payment.entity;

/** 구독 업그레이드 실패 기록의 처리 상태입니다. */
public enum FailedSubscriptionUpgradeStatus {
    /** 구독 업그레이드 재시도 대기 상태입니다. */
    PENDING,
    /** 구독 업그레이드 재시도 성공 또는 재시도 불필요 상태입니다. */
    RESOLVED,
    /** 최대 재시도 횟수에 도달해 포기한 상태입니다. */
    GIVEN_UP
}
