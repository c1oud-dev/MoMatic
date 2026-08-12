package com.momatic.domain.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.domain.payment.dto.PaymentWebhookRequest;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.entity.PaymentStatus;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.toss.TossPaymentClient;
import com.momatic.infra.toss.TossPaymentNetworkException;
import com.momatic.infra.toss.TossPaymentResponse;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String EMAIL = "payer@example.com";
    private static final String ORDER_ID = "order-1";
    private static final String PAYMENT_KEY = "payment-key-1";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(19_900L);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentConfirmProcessor paymentConfirmProcessor;

    @Mock
    private SubscriptionUpgradeRetryService subscriptionUpgradeRetryService;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = User.create(
                EMAIL,
                "결제 사용자",
                "ROLE_USER",
                "google",
                "provider-id"
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        payment = Payment.createPending(
                ORDER_ID,
                AMOUNT,
                PlanPolicy.PRO,
                user
        );
        ReflectionTestUtils.setField(payment, "id", 10L);
    }

    @Test
    @DisplayName("정상 승인 시 결제가 완료 상태로 변경되고 구독이 업그레이드된다")
    void confirmCompletesPaymentAndUpgradesSubscription() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        TossPaymentResponse response = new TossPaymentResponse(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT,
                "DONE"
        );
        payment.markProcessing();
        when(paymentConfirmProcessor.claim(EMAIL, request)).thenReturn(payment);
        when(tossPaymentClient.confirm(request)).thenReturn(response);
        when(paymentConfirmProcessor.complete(payment.getId(), request, response))
                .thenAnswer(invocation -> {
                    payment.complete(PAYMENT_KEY);
                    return payment;
                });

        // when
        Payment result = paymentService.confirm(EMAIL, request);

        // then
        assertEquals(PaymentStatus.DONE, result.getStatus());
        assertEquals(PAYMENT_KEY, result.getPaymentKey());
        verify(paymentConfirmProcessor).upgradeSubscription(payment.getId());
    }

    @Test
    @DisplayName("이미 승인 완료된 결제에 동일한 paymentKey로 재요청하면 토스 API를 재호출하지 않고 기존 결제를 반환한다")
    void confirmReturnsCompletedPaymentForSamePaymentKey() {
        // given
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
        ReflectionTestUtils.setField(payment, "paymentKey", PAYMENT_KEY);
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        when(paymentConfirmProcessor.claim(EMAIL, request)).thenReturn(payment);

        // when
        Payment result = paymentService.confirm(EMAIL, request);

        // then
        assertSame(payment, result);
        verify(tossPaymentClient, never()).confirm(any());
    }

    @Test
    @DisplayName("이미 승인 완료된 결제에 다른 paymentKey로 재요청하면 예외가 발생한다")
    void confirmThrowsWhenCompletedPaymentHasDifferentPaymentKey() {
        // given
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
        ReflectionTestUtils.setField(payment, "paymentKey", "key-A");
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                "key-B",
                AMOUNT
        );
        when(paymentConfirmProcessor.claim(EMAIL, request))
                .thenThrow(new CustomException(ErrorCode.INVALID_PAYMENT_STATUS));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm(EMAIL, request)
        );

        // then
        assertEquals(ErrorCode.INVALID_PAYMENT_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("요청 금액이 결제 금액과 다르면 예외가 발생한다")
    void confirmThrowsWhenAmountDoesNotMatch() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT.add(BigDecimal.ONE)
        );
        when(paymentConfirmProcessor.claim(EMAIL, request))
                .thenThrow(new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm(EMAIL, request)
        );

        // then
        assertEquals(ErrorCode.INVALID_PAYMENT_AMOUNT, exception.getErrorCode());
        verify(tossPaymentClient, never()).confirm(any());
    }

    @Test
    @DisplayName("결제 소유자가 아닌 사용자가 승인 요청하면 예외가 발생한다")
    void confirmThrowsWhenRequesterIsNotPaymentOwner() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        when(paymentConfirmProcessor.claim("other@example.com", request))
                .thenThrow(new CustomException(ErrorCode.FORBIDDEN));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm("other@example.com", request)
        );

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 선점에 실패하면 처리 중 예외가 발생하고 토스 API를 호출하지 않는다")
    void confirmDoesNotCallTossWhenClaimFails() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        when(paymentConfirmProcessor.claim(EMAIL, request))
                .thenThrow(new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSING));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm(EMAIL, request)
        );

        // then
        assertEquals(ErrorCode.PAYMENT_ALREADY_PROCESSING, exception.getErrorCode());
        verifyNoInteractions(tossPaymentClient);
    }

    @Test
    @DisplayName("구독 업그레이드가 실패해도 완료된 결제를 반환한다")
    void confirmKeepsDoneWhenSubscriptionUpgradeFails() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        TossPaymentResponse response = new TossPaymentResponse(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT,
                "DONE"
        );
        payment.markProcessing();
        when(paymentConfirmProcessor.claim(EMAIL, request)).thenReturn(payment);
        when(tossPaymentClient.confirm(request)).thenReturn(response);
        when(paymentConfirmProcessor.complete(payment.getId(), request, response))
                .thenAnswer(invocation -> {
                    payment.complete(PAYMENT_KEY);
                    return payment;
                });
        doThrow(new RuntimeException("DB 오류"))
                .when(paymentConfirmProcessor)
                .upgradeSubscription(payment.getId());

        // when
        Payment result = paymentService.confirm(EMAIL, request);

        // then
        assertEquals(PaymentStatus.DONE, result.getStatus());
        verify(subscriptionUpgradeRetryService).recordFailure(payment.getId(), "DB 오류");
    }

    @Test
    @DisplayName("승인 호출 타임아웃 후 조회 결과가 완료이면 결제를 완료한다")
    void confirmCompletesWhenLookupAfterTimeoutIsDone() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        TossPaymentResponse response = new TossPaymentResponse(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT,
                "DONE"
        );
        payment.markProcessing();
        when(paymentConfirmProcessor.claim(EMAIL, request)).thenReturn(payment);
        when(tossPaymentClient.confirm(request))
                .thenThrow(new TossPaymentNetworkException(new java.io.IOException()));
        when(tossPaymentClient.findByOrderId(ORDER_ID)).thenReturn(Optional.of(response));
        when(paymentConfirmProcessor.complete(payment.getId(), request, response))
                .thenAnswer(invocation -> {
                    payment.complete(PAYMENT_KEY);
                    return payment;
                });

        // when
        Payment result = paymentService.confirm(EMAIL, request);

        // then
        assertEquals(PaymentStatus.DONE, result.getStatus());
    }

    @Test
    @DisplayName("승인 호출 타임아웃 후 조회 결과가 완료가 아니면 대기 상태로 복구한다")
    void confirmRestoresPendingWhenLookupAfterTimeoutIsNotDone() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PAYMENT_KEY,
                AMOUNT
        );
        payment.markProcessing();
        when(paymentConfirmProcessor.claim(EMAIL, request)).thenReturn(payment);
        when(tossPaymentClient.confirm(request))
                .thenThrow(new TossPaymentNetworkException(new java.io.IOException()));
        when(tossPaymentClient.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm(EMAIL, request)
        );

        // then
        assertEquals(ErrorCode.PAYMENT_CONFIRM_FAILED, exception.getErrorCode());
        verify(paymentConfirmProcessor).restoreToPending(payment.getId());
    }

    @Test
    @DisplayName("처리 중 결제에 완료 Webhook이 오면 정상적으로 완료한다")
    void handleWebhookCompletesProcessingPayment() {
        // given
        payment.markProcessing();
        PaymentWebhookRequest request = webhookRequest("DONE", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.DONE, payment.getStatus());
        verify(subscriptionService).upgrade(user.getId(), payment.getPlanType());
    }

    @Test
    @DisplayName("결제 상태 변경 Webhook의 DONE 상태는 결제를 완료하고 구독을 업그레이드한다")
    void handleWebhookCompletesPaymentAndUpgradesSubscription() {
        // given
        PaymentWebhookRequest request = webhookRequest("DONE", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.DONE, payment.getStatus());
        verify(subscriptionService).upgrade(user.getId(), payment.getPlanType());
    }

    @Test
    @DisplayName("결제 상태 변경 Webhook의 EXPIRED 상태는 결제를 실패 처리한다")
    void handleWebhookMarksPendingPaymentAsFailed() {
        // given
        PaymentWebhookRequest request = webhookRequest("EXPIRED", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    @DisplayName("결제 상태 변경 Webhook의 CANCELED 상태는 결제를 취소하고 구독을 취소한다")
    void handleWebhookCancelsCompletedPaymentAndSubscription() {
        // given
        PaymentWebhookRequest request = webhookRequest("CANCELED", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(subscriptionService).cancelActiveSubscription(user.getId());
    }

    @Test
    @DisplayName("결제 상태 변경 Webhook의 READY 상태는 결제를 변경하지 않는다")
    void handleWebhookIgnoresReadyStatus() {
        // given
        PaymentWebhookRequest request = webhookRequest("READY", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        assertDoesNotThrow(() -> paymentService.handleWebhook(request));

        // then
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("알 수 없는 Webhook 이벤트 타입은 예외 없이 무시한다")
    void handleWebhookIgnoresUnknownEventType() {
        // given
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "UNKNOWN_EVENT",
                "2026-08-11T10:00:00.000000",
                null
        );

        // when
        assertDoesNotThrow(() -> paymentService.handleWebhook(request));

        // then
        verifyNoInteractions(paymentRepository, subscriptionService);
    }

    @Test
    @DisplayName("DONE 상태의 Webhook 금액이 주문 금액과 다르면 예외가 발생한다")
    void handleWebhookThrowsWhenDoneAmountDoesNotMatch() {
        // given
        PaymentWebhookRequest request = webhookRequest(
                "DONE",
                AMOUNT.add(BigDecimal.ONE)
        );
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.handleWebhook(request)
        );

        // then
        assertEquals(ErrorCode.INVALID_PAYMENT_AMOUNT, exception.getErrorCode());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("이미 완료된 결제의 DONE Webhook은 구독을 다시 업그레이드하지 않는다")
    void handleWebhookDoesNotUpgradeAlreadyCompletedPayment() {
        // given
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
        ReflectionTestUtils.setField(payment, "paymentKey", PAYMENT_KEY);
        PaymentWebhookRequest request = webhookRequest("DONE", AMOUNT);
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.DONE, payment.getStatus());
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("FREE 플랜으로 결제 생성을 요청하면 예외가 발생한다")
    void createPendingPaymentThrowsForFreePlan() {
        // given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.createPendingPayment(
                        EMAIL,
                        PlanPolicy.FREE.name()
                )
        );

        // then
        assertEquals(ErrorCode.INVALID_PLAN_TYPE, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    /**
     * 결제 상태 변경 Webhook 테스트 요청을 생성합니다.
     *
     * @param status 토스페이먼츠 결제 상태
     * @param totalAmount 총 결제 금액
     * @return Webhook 테스트 요청
     */
    private PaymentWebhookRequest webhookRequest(String status,
                                                 BigDecimal totalAmount) {
        return new PaymentWebhookRequest(
                "PAYMENT_STATUS_CHANGED",
                "2026-08-11T10:00:00.000000",
                new TossPaymentResponse(
                        ORDER_ID,
                        PAYMENT_KEY,
                        totalAmount,
                        status
                )
        );
    }
}