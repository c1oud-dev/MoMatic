package com.momatic.infra.toss;

/** 토스페이먼츠 호출 결과를 확정할 수 없는 네트워크 예외입니다. */
public class TossPaymentNetworkException extends RuntimeException {

    /**
     * 네트워크 예외를 생성합니다.
     *
     * @param cause 원인이 된 입출력 예외
     */
    public TossPaymentNetworkException(Throwable cause) {
        super(cause);
    }
}
