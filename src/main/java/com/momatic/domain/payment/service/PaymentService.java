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
import com.momatic.infra.toss.TossPaymentResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 결제 주문 생성, 승인, Webhook 상태 변경을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final TossPaymentClient tossPaymentClient;

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
    @Transactional
    public Payment confirm(String email,
                           PaymentConfirmRequest request) {
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        Payment payment = getPendingPayment(request.orderId());
        validateOwner(payment, email);
        validatePaymentRequest(payment, request);
        TossPaymentResponse tossResponse = tossPaymentClient.confirm(request);
        validateTossResponse(payment, request, tossResponse);
        payment.complete(tossResponse.paymentKey());
        subscriptionService.upgrade(payment.getUser().getId(), payment.getPlanType());
        return payment;
    }

    /**
     * Webhook 이벤트에 따라 결제와 구독 상태를 변경합니다.
     *
     * @param request Webhook 요청
     */
    @Transactional
    public void handleWebhook(PaymentWebhookRequest request) {
        if (request == null || request.data() == null || request.eventType() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        Payment payment = findWebhookPayment(request.data());
        switch (request.eventType()) {
            case "PAYMENT_DONE" -> completeFromWebhook(payment, request.data().paymentKey());
            case "PAYMENT_FAILED" -> failFromWebhook(payment);
            case "PAYMENT_CANCELED" -> cancelFromWebhook(payment);
            default -> throw new CustomException(ErrorCode.INVALID_PAYMENT_WEBHOOK);
        }
    }

    /**
     * 사용자의 결제 내역을 최신순으로 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 결제 내역
     */
    @Transactional(readOnly = true)
    public List<Payment> getPayments(String email) {
        return paymentRepository.findAllByUserIdOrderByCreatedAtDesc(findUser(email).getId());
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
     * @param paymentKey 토스페이먼츠 결제 키
     */
    private void completeFromWebhook(Payment payment,
                                     String paymentKey) {
        if (payment.getStatus() == PaymentStatus.DONE) {
            return;
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_WEBHOOK);
        }
        payment.complete(paymentKey);
        subscriptionService.upgrade(payment.getUser().getId(), payment.getPlanType());
    }

    /**
     * Webhook 승인 실패 이벤트를 반영합니다.
     *
     * @param payment 결제 엔티티
     */
    private void failFromWebhook(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PENDING) {
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
        payment.cancel();
        subscriptionService.expireActiveSubscription(payment.getUser().getId());
    }

    /**
     * Webhook 데이터에 해당하는 결제를 조회합니다.
     *
     * @param data Webhook 결제 데이터
     * @return 결제 엔티티
     */
    private Payment findWebhookPayment(PaymentWebhookRequest.PaymentWebhookData data) {
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
     * 주문 ID에 해당하는 승인 대기 결제를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 승인 대기 결제
     */
    private Payment getPendingPayment(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        return payment;
    }

    /**
     * 결제 요청 사용자가 주문 소유자인지 검증합니다.
     *
     * @param payment 결제 엔티티
     * @param email 사용자 이메일
     */
    private void validateOwner(Payment payment,
                               String email) {
        if (!payment.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 브라우저가 전달한 승인 값이 서버 주문과 일치하는지 검증합니다.
     *
     * @param payment 결제 엔티티
     * @param request 결제 승인 요청
     */
    private void validatePaymentRequest(Payment payment,
                                        PaymentConfirmRequest request) {
        if (request.paymentKey() == null
                || request.paymentKey().isBlank()
                || request.amount() == null
                || payment.getAmount().compareTo(request.amount()) != 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    /**
     * 토스페이먼츠 승인 응답이 서버 주문과 일치하는지 검증합니다.
     *
     * @param payment 결제 엔티티
     * @param request 결제 승인 요청
     * @param response 토스페이먼츠 승인 응답
     */
    private void validateTossResponse(Payment payment,
                                      PaymentConfirmRequest request,
                                      TossPaymentResponse response) {
        if (!payment.getOrderId().equals(response.orderId())
                || !request.paymentKey().equals(response.paymentKey())
                || !"DONE".equals(response.status())
                || response.totalAmount() == null
                || payment.getAmount().compareTo(response.totalAmount()) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
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