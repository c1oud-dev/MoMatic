package com.momatic.domain.payment.repository;

import com.momatic.domain.payment.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.momatic.domain.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 결제 이력 조회와 저장을 위한 레포지토리입니다. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 승인 대기 결제를 원자적으로 처리 중 상태로 선점합니다.
     *
     * @param paymentId 결제 ID
     * @return 변경된 행 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Payment payment
               set payment.status = com.momatic.domain.payment.entity.PaymentStatus.PROCESSING
             where payment.id = :paymentId
               and payment.status = com.momatic.domain.payment.entity.PaymentStatus.PENDING
            """)
    int markProcessing(@Param("paymentId") Long paymentId);

    /**
     * 기준 시각 이전부터 특정 상태에 머문 결제를 조회합니다.
     *
     * @param status 결제 상태
     * @param threshold 수정 시각 기준
     * @return 정체 결제 목록
     */
    List<Payment> findAllByStatusAndUpdatedAtBefore(PaymentStatus status,
                                                    LocalDateTime threshold);

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
     * @param pageable 페이징 정보
     * @return 결제 이력 페이지
     */
    Page<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId,
                                                      Pageable pageable);

    /**
     * 사용자 ID에 해당하는 결제 이력을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
}
