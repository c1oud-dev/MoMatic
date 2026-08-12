package com.momatic.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.momatic.domain.payment.entity.FailedSubscriptionUpgrade;
import com.momatic.domain.payment.entity.FailedSubscriptionUpgradeStatus;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.repository.FailedSubscriptionUpgradeRepository;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 구독 업그레이드 재시도 서비스의 동작을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionUpgradeRetryServiceTest {

    private static final Long PAYMENT_ID = 10L;
    private static final Long USER_ID = 1L;

    @Mock
    private FailedSubscriptionUpgradeRepository failedSubscriptionUpgradeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionUpgradeRetryService subscriptionUpgradeRetryService;

    private Payment payment;

    /** 각 테스트에서 사용할 승인 완료 결제를 생성합니다. */
    @BeforeEach
    void setUp() {
        User user = User.create(
                "payer@example.com",
                "결제 사용자",
                "ROLE_USER",
                "google",
                "provider-id"
        );
        ReflectionTestUtils.setField(user, "id", USER_ID);
        payment = Payment.createPending(
                "order-1",
                BigDecimal.valueOf(19_900L),
                PlanPolicy.PRO,
                user
        );
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);
        payment.complete("payment-key-1");
    }

    /** 대기 기록이 이미 존재할 때 중복 저장하지 않는지 검증합니다. */
    @Test
    @DisplayName("이미 PENDING 기록이 있으면 중복 저장하지 않는다")
    void recordFailureDoesNotSaveDuplicatePendingRecord() {
        // given
        when(failedSubscriptionUpgradeRepository.existsByPaymentIdAndStatus(
                PAYMENT_ID,
                FailedSubscriptionUpgradeStatus.PENDING
        )).thenReturn(true);

        // when
        subscriptionUpgradeRetryService.recordFailure(PAYMENT_ID, "DB 오류");

        // then
        verify(failedSubscriptionUpgradeRepository, never()).save(
                org.mockito.ArgumentMatchers.any(FailedSubscriptionUpgrade.class)
        );
    }

    /** 재시도 성공 시 실패 기록을 해결 상태로 변경하는지 검증합니다. */
    @Test
    @DisplayName("재시도 성공 시 RESOLVED로 변경된다")
    void retryPendingMarksRecordResolvedOnSuccess() {
        // given
        FailedSubscriptionUpgrade failure = FailedSubscriptionUpgrade.create(PAYMENT_ID);
        when(failedSubscriptionUpgradeRepository.findAllByStatus(
                FailedSubscriptionUpgradeStatus.PENDING
        )).thenReturn(List.of(failure));
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // when
        subscriptionUpgradeRetryService.retryPending();

        // then
        verify(subscriptionService).upgrade(USER_ID, PlanPolicy.PRO);
        assertEquals(FailedSubscriptionUpgradeStatus.RESOLVED, failure.getStatus());
    }

    /** 완료되지 않은 결제는 구독을 변경하지 않고 큐에서 제거하는지 검증합니다. */
    @Test
    @DisplayName("결제가 DONE이 아니면 재시도하지 않고 RESOLVED로 제거된다")
    void retryPendingResolvesRecordWhenPaymentIsNotDone() {
        // given
        payment.cancel();
        FailedSubscriptionUpgrade failure = FailedSubscriptionUpgrade.create(PAYMENT_ID);
        when(failedSubscriptionUpgradeRepository.findAllByStatus(
                FailedSubscriptionUpgradeStatus.PENDING
        )).thenReturn(List.of(failure));
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // when
        subscriptionUpgradeRetryService.retryPending();

        // then
        verify(subscriptionService, never()).upgrade(USER_ID, PlanPolicy.PRO);
        assertEquals(FailedSubscriptionUpgradeStatus.RESOLVED, failure.getStatus());
    }

    /** 다섯 번째 재시도 실패 시 포기 상태로 변경하는지 검증합니다. */
    @Test
    @DisplayName("최대 재시도 횟수 도달 시 GIVEN_UP으로 변경된다")
    void retryPendingMarksRecordGivenUpAtMaximumRetryCount() {
        // given
        FailedSubscriptionUpgrade failure = FailedSubscriptionUpgrade.create(PAYMENT_ID);
        for (int retryCount = 0; retryCount < 4; retryCount++) {
            failure.recordFailedAttempt(5, "이전 오류");
        }
        when(failedSubscriptionUpgradeRepository.findAllByStatus(
                FailedSubscriptionUpgradeStatus.PENDING
        )).thenReturn(List.of(failure));
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        org.mockito.Mockito.doThrow(new RuntimeException("DB 오류"))
                .when(subscriptionService)
                .upgrade(USER_ID, PlanPolicy.PRO);

        // when
        subscriptionUpgradeRetryService.retryPending();

        // then
        assertEquals(5, failure.getRetryCount());
        assertEquals(FailedSubscriptionUpgradeStatus.GIVEN_UP, failure.getStatus());
        assertEquals("DB 오류", failure.getLastErrorMessage());
    }
}