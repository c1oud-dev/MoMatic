package com.momatic.domain.payment.entity;

/** 토스페이먼츠 Webhook 이벤트의 처리 상태입니다. */
public enum WebhookEventStatus {

    /** 이벤트를 수신하여 처리 중인 상태입니다. */
    RECEIVED,

    /** 이벤트 처리를 완료하여 동일 이벤트의 중복 처리를 차단하는 상태입니다. */
    PROCESSED,

    /** 이벤트 처리에 실패하여 토스페이먼츠의 재전송을 기다리는 상태입니다. */
    FAILED,

    /** 재전송을 모두 소진해 수동 확인이 필요한 상태입니다. */
    GIVEN_UP
}
