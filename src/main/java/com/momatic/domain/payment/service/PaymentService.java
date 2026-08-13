package com.momatic.domain.payment.service;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 결제 주문 생성, 승인, Webhook 상태 변경을 처리하는 서비스입니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentConfirmProcessor paymentConfirmProcessor;
    private final SubscriptionUpgradeRetryService subscriptionUpgradeRetryService;

    /**
     * 결제창에 전달할 승인 대기 주문을 생성합니다.
     *
     * @param email 결제 사용자 이메일
     * @param planType 결제 플랜 문자열
     * @return 생성된 승인 대기 결제
     */
    @Transactional
    public Payment createPendingPayment(String email,
                                        String planType) {
        User user = findUser(email);
        PlanPolicy planPolicy = PlanPolicy.from(planType);
        if (planPolicy == PlanPolicy.FREE) {
            throw new CustomException(ErrorCode.INVALID_PLAN_TYPE);
        }
        return paymentRepository.save(Payment.createPending(
                UUID.randomUUID().toString(),
                getPlanAmount(planPolicy),
                planPolicy,
                user
        ));
    }

    /**
     * 사용자가 요청한 결제를 토스페이먼츠에서 승인하고 구독을 업그레이드합니다.
     *
     * @param email 결제 사용자 이메일
     * @param request 결제 승인 요청
     * @return 승인 완료 결제
     */
    public Payment confirm(String email,
                           PaymentConfirmRequest request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Payment payment = paymentConfirmProcessor.claim(email, request);

        if (payment.getStatus() == PaymentStatus.DONE) {
            return payment;
        }

        TossPaymentResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirm(request);
        } catch (TossPaymentNetworkException exception) {
            return resolveAmbiguousConfirmation(payment, request, exception);
        } catch (CustomException exception) {
            paymentConfirmProcessor.restoreToPending(payment.getId());
            throw exception;
        }

        Payment completed = paymentConfirmProcessor.complete(
                payment.getId(),
                request,
                tossResponse
        );
        upgradeSubscriptionSafely(completed.getId());
        return completed;
    }

    /**
     * 네트워크 오류 뒤 토스 결제 상태를 조회하여 승인 결과를 확정합니다.
     *
     * @param payment 처리 중 결제
     * @param request 최초 승인 요청
     * @param cause 승인 호출 네트워크 예외
     * @return 토스에서 완료된 것으로 확인된 결제
     */
    private Payment resolveAmbiguousConfirmation(Payment payment,
                                                 PaymentConfirmRequest request,
                                                 TossPaymentNetworkException cause) {
        log.warn(
                "토스 결제 승인 결과가 불명확하여 상태를 조회합니다: orderId={}",
                payment.getOrderId(),
                cause
        );
        TossPaymentResponse response;
        try {
            response = tossPaymentClient
                    .findByOrderId(payment.getOrderId())
                    .orElse(null);
        } catch (TossPaymentNetworkException lookupException) {
            log.error(
                    "토스 결제 승인 결과를 확인하지 못했습니다: orderId={}",
                    payment.getOrderId(),
                    lookupException
            );
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        if (response != null && "DONE".equals(response.status())) {
            Payment completed = paymentConfirmProcessor.complete(
                    payment.getId(),
                    request,
                    response
            );
            upgradeSubscriptionSafely(completed.getId());
            return completed;
        }

        paymentConfirmProcessor.restoreToPending(payment.getId());
        throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
    }

    /**
     * 결제 완료 후 구독 업그레이드를 시도하고 실패를 기록합니다.
     *
     * @param paymentId 결제 ID
     */
    private void upgradeSubscriptionSafely(Long paymentId) {
        try {
            paymentConfirmProcessor.upgradeSubscription(paymentId);
        } catch (RuntimeException exception) {
            log.error("결제 완료 후 구독 업그레이드에 실패했습니다: paymentId={}",
                    paymentId,
                    exception);
            recordSubscriptionUpgradeFailure(paymentId, exception);
        }
    }

    /**
     * 구독 업그레이드 실패를 재시도 큐에 기록하되 기록 실패를 전파하지 않습니다.
     *
     * @param paymentId 결제 ID
     * @param cause 구독 업그레이드 실패 원인
     */
    private void recordSubscriptionUpgradeFailure(Long paymentId,
                                                  RuntimeException cause) {
        try {
            subscriptionUpgradeRetryService.recordFailure(paymentId, cause.getMessage());
        } catch (RuntimeException recordException) {
            log.error(
                    "구독 업그레이드 재시도 기록 저장에 실패했습니다: paymentId={}",
                    paymentId,
                    recordException
            );
        }
    }

    /**
     * Webhook 이벤트에 따라 결제와 구독 상태를 변경합니다.
     *
     * @param request Webhook 요청
     */
    @Transactional
    public void handleWebhook(PaymentWebhookRequest request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (!"PAYMENT_STATUS_CHANGED".equals(request.eventType())) {
            log.warn("처리 대상이 아닌 토스페이먼츠 Webhook 이벤트입니다: eventType={}",
                    request.eventType());
            return;
        }
        if (request.data() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (request.data().status() == null) {
            log.warn("처리할 수 없는 토스페이먼츠 결제 상태입니다: orderId={}, status=null",
                    request.data().orderId());
            return;
        }

        Payment payment = findWebhookPayment(request.data());
        switch (request.data().status()) {
            case "DONE" -> completeFromWebhook(payment, request.data());
            case "CANCELED", "PARTIAL_CANCELED" -> cancelFromWebhook(payment);
            case "EXPIRED", "ABORTED" -> failFromWebhook(payment);
            case "READY", "IN_PROGRESS", "WAITING_FOR_DEPOSIT" -> {
                // 정상적인 중간 상태이므로 후속 Webhook을 기다립니다.
            }
            default -> log.warn(
                    "처리할 수 없는 토스페이먼츠 결제 상태입니다: orderId={}, status={}",
                    request.data().orderId(),
                    request.data().status()
            );
        }
    }

    /**
     * 사용자의 결제 내역을 최신순으로 조회합니다.
     *
     * @param email 사용자 이메일
     * @param pageable 페이징 정보
     * @return 결제 내역 페이지
     */
    @Transactional(readOnly = true)
    public Page<Payment> getPayments(String email,
                                     Pageable pageable) {
        return paymentRepository.findAllByUserIdOrderByCreatedAtDesc(
                findUser(email).getId(),
                pageable
        );
    }

    /**
     * 플랜의 결제 금액을 조회합니다.
     *
     * @param planPolicy 플랜 정책
     * @return 결제 금액
     */
    public BigDecimal getPlanAmount(PlanPolicy planPolicy) {
        return planPolicy.getPrice();
    }

    /**
     * Webhook 승인 완료 이벤트를 반영합니다.
     *
     * @param payment 결제 엔티티
     * @param data 토스페이먼츠 결제 객체
     */
    private void completeFromWebhook(Payment payment,
                                     TossPaymentResponse data) {
        if (payment.getStatus() == PaymentStatus.DONE) {
            return;
        }
        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.PROCESSING) {
            return;
        }
        if (data.paymentKey() == null || data.paymentKey().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_WEBHOOK);
        }
        if (data.totalAmount() == null
                || payment.getAmount().compareTo(data.totalAmount()) != 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        payment.complete(data.paymentKey());
        subscriptionService.upgrade(payment.getUser().getId(), payment.getPlanType());
    }

    /**
     * Webhook 승인 실패 이벤트를 반영합니다.
     *
     * @param payment 결제 엔티티
     */
    private void failFromWebhook(Payment payment) {
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.PENDING
                || payment.getStatus() == PaymentStatus.PROCESSING) {
            payment.fail();
        }
    }

    /**
     * Webhook 취소 이벤트를 반영합니다.
     *
     * @param payment 결제 엔티티
     */
    private void cancelFromWebhook(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.PROCESSING
                && payment.getStatus() != PaymentStatus.DONE) {
            return;
        }

        payment.cancel();
        subscriptionService.cancelActiveSubscription(payment.getUser().getId());
    }

    /**
     * Webhook 데이터에 해당하는 결제를 조회합니다.
     *
     * @param data Webhook 결제 데이터
     * @return 결제 엔티티
     */
    private Payment findWebhookPayment(TossPaymentResponse data) {
        if (data.orderId() != null && !data.orderId().isBlank()) {
            return paymentRepository.findByOrderId(data.orderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        }
        if (data.paymentKey() != null && !data.paymentKey().isBlank()) {
            return paymentRepository.findByPaymentKey(data.paymentKey())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        }
        throw new CustomException(ErrorCode.INVALID_PAYMENT_WEBHOOK);
    }

    /**
     * 이메일에 해당하는 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}