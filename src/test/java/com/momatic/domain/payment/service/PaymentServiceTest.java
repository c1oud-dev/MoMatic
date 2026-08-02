package com.momatic.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));
        when(tossPaymentClient.confirm(request)).thenReturn(response);

        // when
        Payment result = paymentService.confirm(EMAIL, request);

        // then
        assertEquals(PaymentStatus.DONE, result.getStatus());
        assertEquals(PAYMENT_KEY, result.getPaymentKey());
        verify(subscriptionService).upgrade(user.getId(), payment.getPlanType());
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
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

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
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

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
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

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
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentService.confirm("other@example.com", request)
        );

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("PAYMENT_DONE 이벤트 수신 시 PENDING 결제가 완료 처리되고 구독이 업그레이드된다")
    void handleWebhookCompletesPaymentAndUpgradesSubscription() {
        // given
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "PAYMENT_DONE",
                new PaymentWebhookRequest.PaymentWebhookData(
                        ORDER_ID,
                        PAYMENT_KEY
                )
        );
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        verify(subscriptionService).upgrade(user.getId(), payment.getPlanType());
    }

    @Test
    @DisplayName("PAYMENT_FAILED 이벤트 수신 시 PENDING 결제가 실패 처리된다")
    void handleWebhookMarksPendingPaymentAsFailed() {
        // given
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "PAYMENT_FAILED",
                new PaymentWebhookRequest.PaymentWebhookData(
                        ORDER_ID,
                        PAYMENT_KEY
                )
        );
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    @DisplayName("PAYMENT_CANCELED 이벤트 수신 시 완료된 결제가 취소되고 구독이 취소된다")
    void handleWebhookCancelsCompletedPaymentAndSubscription() {
        // given
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                "PAYMENT_CANCELED",
                new PaymentWebhookRequest.PaymentWebhookData(
                        ORDER_ID,
                        PAYMENT_KEY
                )
        );
        when(paymentRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handleWebhook(request);

        // then
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(subscriptionService).cancelActiveSubscription(user.getId());
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
}