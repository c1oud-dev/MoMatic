package com.momatic.domain.payment.entity;

import com.momatic.domain.plan.entity.PlanPolicy;
import com.momatic.domain.user.entity.User;
import com.momatic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 토스페이먼츠 결제 이력을 표현하는 엔티티입니다. */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(unique = true)
    private String paymentKey;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanPolicy planType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 승인 대기 결제를 생성합니다.
     *
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param planType 결제 플랜
     * @param user 결제 사용자
     * @return 승인 대기 결제
     */
    public static Payment createPending(String orderId,
                                        BigDecimal amount,
                                        PlanPolicy planType,
                                        User user) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.planType = planType;
        payment.user = user;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    /**
     * 결제를 승인 완료 상태로 변경합니다.
     *
     * @param paymentKey 토스페이먼츠 결제 키
     */
    public void complete(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.DONE;
    }

    /** 결제를 실패 상태로 변경합니다. */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    /** 결제를 취소 상태로 변경합니다. */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
