package com.momatic.domain.payment.service;

import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.domain.payment.entity.Payment;
import com.momatic.domain.payment.entity.PaymentStatus;
import com.momatic.domain.payment.repository.PaymentRepository;
import com.momatic.domain.subscription.service.SubscriptionService;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import com.momatic.infra.toss.TossPaymentResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 결제 승인의 짧은 데이터베이스 트랜잭션 단계를 처리합니다. */
@Service
@RequiredArgsConstructor
public class PaymentConfirmProcessor {

    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 결제 요청을 검증하고 승인 처리 상태를 원자적으로 선점합니다.
     *
     * @param email 결제 사용자 이메일
     * @param request 결제 승인 요청
     * @return 선점되었거나 이미 완료된 결제
     */
    @Transactional
    public Payment claim(String email,
                         PaymentConfirmRequest request) {
        Payment payment = findByOrderId(request.orderId());
        validateOwner(payment, email);
        validatePaymentRequest(payment, request);
        if (payment.getStatus() == PaymentStatus.DONE) {
            validateCompletedPayment(payment, request);
            return payment;
        }
        if (payment.getStatus() == PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSING);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        if (paymentRepository.markProcessing(payment.getId()) == 0) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSING);
        }
        return paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 토스 승인 응답을 검증하고 결제를 완료 상태로 확정합니다.
     *
     * @param paymentId 결제 ID
     * @param request 최초 승인 요청
     * @param response 토스 결제 응답
     * @return 완료된 결제
     */
    @Transactional
    public Payment complete(Long paymentId,
                            PaymentConfirmRequest request,
                            TossPaymentResponse response) {
        Payment payment = findById(paymentId);
        if (payment.getStatus() == PaymentStatus.DONE) {
            return payment;
        }
        validateTossResponse(payment, request.paymentKey(), response);
        payment.complete(response.paymentKey());
        return payment;
    }

    /**
     * 조회 응답을 검증하고 정체 결제를 완료 상태로 확정합니다.
     *
     * @param paymentId 결제 ID
     * @param response 토스 결제 조회 응답
     * @return 완료된 결제
     */
    @Transactional
    public Payment completeRecovered(Long paymentId,
                                     TossPaymentResponse response) {
        Payment payment = findById(paymentId);
        if (payment.getStatus() == PaymentStatus.DONE) {
            return payment;
        }
        validateTossResponse(payment, response.paymentKey(), response);
        payment.complete(response.paymentKey());
        return payment;
    }

    /**
     * 처리 중인 결제를 승인 대기 상태로 복구합니다.
     *
     * @param paymentId 결제 ID
     */
    @Transactional
    public void restoreToPending(Long paymentId) {
        findById(paymentId).restoreToPending();
    }

    /**
     * 기준 시각 이전부터 처리 중인 결제를 조회합니다.
     *
     * @param threshold 수정 시각 기준
     * @return 정체 결제 목록
     */
    @Transactional(readOnly = true)
    public List<Payment> findStaleProcessingPayments(LocalDateTime threshold) {
        return paymentRepository.findAllByStatusAndUpdatedAtBefore(
                PaymentStatus.PROCESSING,
                threshold
        );
    }

    /**
     * 결제에 해당하는 구독을 별도 트랜잭션에서 업그레이드합니다.
     *
     * @param paymentId 결제 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upgradeSubscription(Long paymentId) {
        Payment payment = findById(paymentId);
        subscriptionService.upgrade(payment.getUser().getId(), payment.getPlanType());
    }

    /**
     * ID로 결제를 조회합니다.
     *
     * @param paymentId 결제 ID
     * @return 결제 엔티티
     */
    private Payment findById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 주문 ID로 결제를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 결제 엔티티
     */
    private Payment findByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    /** 결제 소유자를 검증합니다. */
    private void validateOwner(Payment payment,
                               String email) {
        if (!payment.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /** 승인 요청의 키와 금액을 검증합니다. */
    private void validatePaymentRequest(Payment payment,
                                        PaymentConfirmRequest request) {
        if (request.paymentKey() == null
                || request.paymentKey().isBlank()
                || request.amount() == null
                || payment.getAmount().compareTo(request.amount()) != 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }

    /** 이미 완료된 결제의 결제 키를 검증합니다. */
    private void validateCompletedPayment(Payment payment,
                                          PaymentConfirmRequest request) {
        if (!request.paymentKey().equals(payment.getPaymentKey())) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    /** 토스 응답의 주문, 결제 키, 상태와 금액을 검증합니다. */
    private void validateTossResponse(Payment payment,
                                      String expectedPaymentKey,
                                      TossPaymentResponse response) {
        if (response == null
                || !payment.getOrderId().equals(response.orderId())
                || expectedPaymentKey == null
                || !expectedPaymentKey.equals(response.paymentKey())
                || !"DONE".equals(response.status())
                || response.totalAmount() == null
                || payment.getAmount().compareTo(response.totalAmount()) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }
}