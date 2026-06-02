package com.momatic.domain.payment.entity;

/** 결제 처리 상태를 정의하는 열거형입니다. */
public enum PaymentStatus {
    PENDING,
    DONE,
    FAILED,
    CANCELLED
}
