package com.momatic.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 실패한 구독 업그레이드의 재시도 상태와 원인을 기록하는 엔티티입니다. */
@Entity
@Table(name = "failed_subscription_upgrade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FailedSubscriptionUpgrade {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FailedSubscriptionUpgradeStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastAttemptAt;

    @Column(length = MAX_ERROR_MESSAGE_LENGTH)
    private String lastErrorMessage;

    /**
     * 구독 업그레이드 실패 기록을 생성합니다.
     *
     * @param paymentId 결제 ID
     * @return 생성된 구독 업그레이드 실패 기록
     */
    public static FailedSubscriptionUpgrade create(Long paymentId) {
        FailedSubscriptionUpgrade failure = new FailedSubscriptionUpgrade();
        failure.paymentId = paymentId;
        failure.status = FailedSubscriptionUpgradeStatus.PENDING;
        failure.retryCount = 0;
        failure.createdAt = LocalDateTime.now();
        return failure;
    }

    /** 구독 업그레이드 실패 기록을 해결 상태로 변경합니다. */
    public void markResolved() {
        this.status = FailedSubscriptionUpgradeStatus.RESOLVED;
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * 최초 구독 업그레이드 실패 원인을 기록합니다.
     *
     * @param errorMessage 실패 원인 메시지
     */
    public void recordInitialError(String errorMessage) {
        this.lastErrorMessage = normalizeErrorMessage(errorMessage);
    }

    /**
     * 구독 업그레이드 재시도 실패 횟수와 원인을 기록합니다.
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param errorMessage 실패 원인 메시지
     */
    public void recordFailedAttempt(int maxRetryCount,
                                    String errorMessage) {
        this.retryCount++;
        this.lastAttemptAt = LocalDateTime.now();
        this.lastErrorMessage = normalizeErrorMessage(errorMessage);
        if (this.retryCount >= maxRetryCount) {
            this.status = FailedSubscriptionUpgradeStatus.GIVEN_UP;
        }
    }

    /**
     * 데이터베이스 컬럼 길이에 맞게 실패 원인 메시지를 정규화합니다.
     *
     * @param errorMessage 실패 원인 메시지
     * @return 저장 가능한 실패 원인 메시지
     */
    private static String normalizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}