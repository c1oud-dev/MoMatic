package com.momatic.domain.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.entity.Subscription;
import com.momatic.domain.subscription.entity.SubscriptionStatus;
import com.momatic.domain.subscription.repository.SubscriptionRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 구독 서비스의 취소 예약 및 즉시 취소 동작을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "subscriber@example.com";

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;

    /** 각 테스트에서 사용할 사용자를 생성합니다. */
    @BeforeEach
    void setUp() {
        user = User.create(EMAIL, "구독 사용자", "ROLE_USER", "google", "provider-id");
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    /** 유료 구독 취소 요청이 플랜과 만료 시각을 유지하는지 검증합니다. */
    @Test
    @DisplayName("유료 구독 취소 요청 시 플랜과 만료 시각을 유지하고 취소 요청 시각만 설정한다")
    void cancelSubscriptionSchedulesCancellationAtPeriodEnd() {
        // given
        Subscription subscription = paidSubscription();
        LocalDateTime expiredAt = subscription.getExpiredAt();
        givenActiveSubscription(subscription);

        // when
        subscriptionService.cancelSubscription(EMAIL);

        // then
        assertEquals(PlanPolicy.PRO, subscription.getPlanType());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(expiredAt, subscription.getExpiredAt());
        assertNotNull(subscription.getCancelRequestedAt());
    }

    /** 무료 플랜의 취소 요청이 거부되는지 검증합니다. */
    @Test
    @DisplayName("FREE 플랜 취소 요청 시 INVALID_REQUEST 예외가 발생한다")
    void cancelSubscriptionRejectsFreePlan() {
        // given
        Subscription subscription = Subscription.createActive(user, PlanPolicy.FREE);
        givenActiveSubscription(subscription);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> subscriptionService.cancelSubscription(EMAIL)
        );

        // then
        assertSame(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    /** 이미 예약된 구독에 대한 중복 취소 요청이 거부되는지 검증합니다. */
    @Test
    @DisplayName("이미 취소 요청된 구독을 다시 취소하면 전용 충돌 예외가 발생한다")
    void cancelSubscriptionRejectsDuplicateRequest() {
        // given
        Subscription subscription = paidSubscription();
        subscription.requestCancel();
        givenActiveSubscription(subscription);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> subscriptionService.cancelSubscription(EMAIL)
        );

        // then
        assertSame(
                ErrorCode.SUBSCRIPTION_ALREADY_CANCEL_REQUESTED,
                exception.getErrorCode()
        );
    }

    /** 구독 취소 예약을 철회하면 요청 시각이 제거되는지 검증합니다. */
    @Test
    @DisplayName("취소 철회 시 cancelRequestedAt이 null이 된다")
    void revokeCancelSubscriptionClearsRequestTime() {
        // given
        Subscription subscription = paidSubscription();
        subscription.requestCancel();
        givenActiveSubscription(subscription);

        // when
        subscriptionService.revokeCancelSubscription(EMAIL);

        // then
        assertNull(subscription.getCancelRequestedAt());
        assertFalse(subscription.isCancelScheduled());
    }

    /** 취소가 예약되지 않은 구독의 철회 요청이 거부되는지 검증합니다. */
    @Test
    @DisplayName("취소 요청 상태가 아닌 구독을 철회하면 INVALID_REQUEST 예외가 발생한다")
    void revokeCancelSubscriptionRejectsUnscheduledSubscription() {
        // given
        Subscription subscription = paidSubscription();
        givenActiveSubscription(subscription);

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> subscriptionService.revokeCancelSubscription(EMAIL)
        );

        // then
        assertSame(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    /** 취소 예정 구독을 업그레이드하면 취소 예약이 해제되는지 검증합니다. */
    @Test
    @DisplayName("취소 예정 상태에서 upgrade하면 cancelRequestedAt이 초기화된다")
    void upgradeClearsScheduledCancellation() {
        // given
        Subscription subscription = paidSubscription();
        subscription.requestCancel();
        assertTrue(subscription.isCancelScheduled());

        // when
        subscription.upgrade(PlanPolicy.TEAM);

        // then
        assertNull(subscription.getCancelRequestedAt());
        assertFalse(subscription.isCancelScheduled());
    }

    /** 결제 취소 Webhook 경로가 구독을 즉시 무료 플랜으로 바꾸는지 검증합니다. */
    @Test
    @DisplayName("Webhook 발 cancelActiveSubscription은 즉시 FREE와 CANCELLED로 변경한다")
    void cancelActiveSubscriptionCancelsImmediately() {
        // given
        Subscription subscription = paidSubscription();
        when(subscriptionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                USER_ID,
                SubscriptionStatus.ACTIVE
        )).thenReturn(Optional.of(subscription));

        // when
        subscriptionService.cancelActiveSubscription(USER_ID);

        // then
        assertEquals(PlanPolicy.FREE, subscription.getPlanType());
        assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
        assertNotNull(subscription.getExpiredAt());
    }

    /** 유료 활성 구독을 생성합니다. */
    private Subscription paidSubscription() {
        Subscription subscription = Subscription.createActive(user, PlanPolicy.PRO);
        subscription.upgrade(PlanPolicy.PRO);
        return subscription;
    }

    /**
     * 이메일 및 사용자 ID 조회 시 주어진 활성 구독이 반환되도록 설정합니다.
     *
     * @param subscription 조회할 활성 구독
     */
    private void givenActiveSubscription(Subscription subscription) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                USER_ID,
                SubscriptionStatus.ACTIVE
        )).thenReturn(Optional.of(subscription));
    }
}