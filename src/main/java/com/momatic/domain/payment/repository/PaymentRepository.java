package com.momatic.domain.payment.repository;

import com.momatic.domain.payment.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 결제 이력 조회와 저장을 위한 레포지토리입니다. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID에 해당하는 결제를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 결제 정보
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * 결제 키에 해당하는 결제를 조회합니다.
     *
     * @param paymentKey 토스페이먼츠 결제 키
     * @return 결제 정보
     */
    Optional<Payment> findByPaymentKey(String paymentKey);

    /**
     * 사용자의 결제 이력을 최신순으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 결제 이력 목록
     */
    List<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
